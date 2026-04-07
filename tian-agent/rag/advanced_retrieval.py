from typing import List, Dict, Optional, Any, Tuple
import hashlib
import json
from collections import defaultdict
from datetime import datetime, timedelta

from langchain_core.documents import Document
from rag.vectore_store import VectorStoreService
from model.factory import chat_model, embed_model
from utils.logger_handler import logger
from utils.config_handler import retrieval_conf, prompts_conf  #
from utils.path_tools import get_abs_path

class QueryCache:
    """查询缓存，避免重复生成扩展查询"""

    def __init__(self, ttl: int = 3600, max_size: int = 1000):
        self.cache = {}
        self.ttl = ttl
        self.max_size = max_size
        self.enabled = retrieval_conf.get("cache", {}).get("enable_query_cache", True)

    def _get_key(self, query: str, strategy: str) -> str:
        """生成缓存键"""
        content = f"{query}:{strategy}"
        return hashlib.md5(content.encode()).hexdigest()

    def get(self, query: str, strategy: str) -> Optional[Any]:
        """获取缓存"""
        if not self.enabled:
            return None

        key = self._get_key(query, strategy)
        if key in self.cache:
            value, timestamp = self.cache[key]
            if datetime.now().timestamp() - timestamp < self.ttl:
                logger.debug(f"[QueryCache] 命中缓存: {strategy} for '{query[:30]}...'")
                return value
            else:
                del self.cache[key]
        return None

    def set(self, query: str, strategy: str, value: Any):
        """设置缓存"""
        if not self.enabled:
            return

        # 清理过期缓存
        self._clean_expired()

        # 如果缓存太大，删除最旧的
        if len(self.cache) >= self.max_size:
            oldest_key = min(self.cache.keys(),
                             key=lambda k: self.cache[k][1])
            del self.cache[oldest_key]

        key = self._get_key(query, strategy)
        self.cache[key] = (value, datetime.now().timestamp())

    def _clean_expired(self):
        """清理过期缓存"""
        now = datetime.now().timestamp()
        expired = [k for k, (_, t) in self.cache.items()
                   if now - t >= self.ttl]
        for k in expired:
            del self.cache[k]


class AdvancedRetrieval:
    """高级检索类，集成多种检索策略"""

    def __init__(self, vector_store: Optional[VectorStoreService] = None):
        self.vector_store = vector_store or VectorStoreService()
        self.retriever = self.vector_store.get_retriever()
        self.cache = QueryCache(
            ttl=retrieval_conf.get("cache", {}).get("cache_ttl", 3600),
            max_size=retrieval_conf.get("cache", {}).get("max_cache_size", 1000)
        )

        # 加载提示词
        self.mqe_prompt = self._load_prompt("mqe_prompt_path")
        self.hyde_prompt = self._load_prompt("hyde_prompt_path")
        self.rewrite_prompt = self._load_prompt("rewrite_prompt_path")

    def _load_prompt(self, prompt_key: str) -> str:
        """加载提示词文件"""
        try:
            path_key = f"{prompt_key}"
            if path_key in prompts_conf:
                path = get_abs_path(prompts_conf[path_key])
                with open(path, "r", encoding="utf-8") as f:
                    return f.read().strip()
        except Exception as e:
            logger.warning(f"[AdvancedRetrieval] 加载提示词失败 {prompt_key}: {e}")

        # 返回默认提示词
        if "mqe" in prompt_key:
            return "根据原始查询：{query}\n请生成{n}个语义等价的扩展查询，每行一个："
        elif "hyde" in prompt_key:
            return "问题：{query}\n请写一段假设性的答案段落用于检索："
        elif "rewrite" in prompt_key:
            return "对话历史：{history}\n当前查询：{current_query}\n改写为独立查询："
        return ""

    def mqe_expand(self, query: str, n: int = 3) -> List[str]:
        """
        多查询扩展(MQE)
        生成多个语义等价的查询
        """
        # 检查缓存
        cached = self.cache.get(query, f"mqe_{n}")
        if cached:
            return cached

        try:
            # 构建提示词
            prompt = self.mqe_prompt.format(query=query, n=n)
            messages = [
                {"role": "system", "content": "你是天津大学校园资料检索用的查询扩展助手。"},
                {"role": "user", "content": prompt}
            ]

            # 调用LLM
            response = chat_model.invoke(messages)

            # 解析结果
            lines = [line.strip("- \t").strip()
                     for line in (response.content or "").splitlines()
                     if line.strip()]

            # 去重并限制数量
            expansions = []
            for line in lines:
                if line and line not in expansions and line != query:
                    expansions.append(line)
                if len(expansions) >= n:
                    break

            # 如果没生成够，补充原始查询
            while len(expansions) < n:
                expansions.append(query)

            # 缓存结果
            self.cache.set(query, f"mqe_{n}", expansions)

            logger.info(f"[MQE] 原始查询: '{query}' -> 扩展查询: {expansions}")
            return expansions

        except Exception as e:
            logger.error(f"[MQE] 生成扩展查询失败: {e}")
            return [query] * n

    def hyde_generate(self, query: str) -> Optional[str]:
        """
        假设文档嵌入(HyDE)
        生成假设性答案文档
        """
        # 检查缓存
        cached = self.cache.get(query, "hyde")
        if cached:
            return cached

        try:
            # 构建提示词
            prompt = self.hyde_prompt.format(query=query)
            messages = [
                {"role": "system", "content": "你是用于天大知识库检索的假设文档生成助手。"},
                {"role": "user", "content": prompt}
            ]

            # 调用LLM
            response = chat_model.invoke(messages)
            hyde_text = response.content.strip()

            if hyde_text:
                # 缓存结果
                self.cache.set(query, "hyde", hyde_text)
                logger.info(f"[HyDE] 生成假设文档: '{hyde_text[:100]}...'")
                return hyde_text

        except Exception as e:
            logger.error(f"[HyDE] 生成假设文档失败: {e}")

        return None

    def rewrite_query(self, current_query: str, history: List[Dict]) -> str:
        """
        多轮对话查询改写
        结合历史上下文改写当前查询
        """
        if not history or len(history) < 2:
            return current_query

        # 检查缓存
        cache_key = f"{current_query}_{len(history)}"
        cached = self.cache.get(cache_key, "rewrite")
        if cached:
            return cached

        try:
            # 格式化历史记录
            history_text = ""
            for msg in history[-retrieval_conf.get("conversation", {}).get("max_history", 3) * 2:]:
                role = "用户" if msg.get("role") == "user" else "助手"
                history_text += f"{role}：{msg.get('content', '')}\n"

            # 构建提示词
            prompt = self.rewrite_prompt.format(
                history=history_text,
                current_query=current_query
            )
            messages = [
                {"role": "system", "content": "你是天津大学校园场景下的查询改写助手。"},
                {"role": "user", "content": prompt}
            ]

            # 调用LLM
            response = chat_model.invoke(messages)
            rewritten = response.content.strip()

            if rewritten and rewritten != current_query:
                # 缓存结果
                self.cache.set(cache_key, "rewrite", rewritten)
                logger.info(f"[QueryRewrite] '{current_query}' -> '{rewritten}'")
                return rewritten

        except Exception as e:
            logger.error(f"[QueryRewrite] 查询改写失败: {e}")

        return current_query

    def search_expanded(
            self,
            query: str,
            top_k: int = 8,
            rag_namespace: Optional[str] = None,
            only_rag_data: bool = True,
            score_threshold: Optional[float] = None,
            enable_mqe: bool = True,
            mqe_expansions: int = 3,
            enable_hyde: bool = True,
            candidate_pool_multiplier: int = 4,
            history: Optional[List[Dict]] = None,
    ) -> List[Document]:
        """
        扩展检索框架
        集成MQE和HyDE，执行多路检索并合并结果
        """
        if not query:
            return []

        # 1. 多轮对话查询改写
        if history and retrieval_conf.get("conversation", {}).get("enable_context", True):
            original_query = query
            query = self.rewrite_query(query, history)
            if query != original_query:
                logger.info(f"[ExpandedSearch] 查询改写: '{original_query}' -> '{query}'")

        # 2. 生成扩展查询集合
        expansions = [query]

        if enable_mqe and mqe_expansions > 0:
            mqe_queries = self.mqe_expand(query, mqe_expansions)
            expansions.extend(mqe_queries)

        if enable_hyde:
            hyde_text = self.hyde_generate(query)
            if hyde_text:
                expansions.append(hyde_text)

        # 去重
        unique_expansions = []
        for exp in expansions:
            if exp and exp not in unique_expansions:
                unique_expansions.append(exp)
        expansions = unique_expansions

        # 3. 计算候选池大小
        base_top_k = top_k
        pool_multiplier = candidate_pool_multiplier
        min_candidates = retrieval_conf.get("expanded", {}).get("min_candidates", 20)

        candidate_pool = max(base_top_k * pool_multiplier, min_candidates)
        per_query_limit = max(1, candidate_pool // len(expansions))

        # 4. 构建过滤器
        # 注意：知识库文档（load_document）通常无 memory_type/is_rag_data/data_source 元数据，
        # 若按这些过滤会返回 0 条。ChromaDB 要求多条件必须用 $and 包裹，如：
        #   {"$and": [{"field": {"$eq": value}}, ...]}
        # 不能使用扁平格式 {"a": 1, "b": 2}，否则会报 "Expected where to have exactly one operator"
        where = None
        if only_rag_data and retrieval_conf.get("retrieval", {}).get("filter_rag_only", False):
            # 仅当配置明确启用且文档有对应元数据时使用
            filter_conditions = [
                {"memory_type": {"$eq": "rag_chunk"}},
                {"is_rag_data": {"$eq": True}},
                {"data_source": {"$eq": "rag_pipeline"}},
            ]
            if rag_namespace:
                filter_conditions.append({"rag_namespace": {"$eq": rag_namespace}})
            where = {"$and": filter_conditions} if len(filter_conditions) > 1 else filter_conditions[0]

        # 5. 多路并行检索
        all_results = {}  # 使用字典去重，key为文档ID

        for exp_query in expansions:
            try:
                # 对每个扩展查询执行检索（显式传 filter 避免默认 filter 覆盖）
                hits = self.vector_store.vector_store.similarity_search_with_score(
                    query=exp_query,
                    k=per_query_limit,
                    filter=where,
                )

                # 合并结果（Chroma 返回的 score 为距离：越小越相似）
                for doc, score in hits:
                    doc_id = doc.metadata.get("memory_id", doc.page_content[:80])
                    if doc_id not in all_results or score < all_results[doc_id][1]:
                        all_results[doc_id] = (doc, score)

            except Exception as e:
                logger.error(f"[ExpandedSearch] 检索失败 '{exp_query[:50]}...': {e}")
                continue

        # 6. 按距离升序排序（距离越小越应优先）
        sorted_results = sorted(
            all_results.values(),
            key=lambda x: x[1],
            reverse=False,
        )

        # 7. 应用相似度阈值：score 为距离时，仅保留 distance <= 阈值 的片段
        if score_threshold is not None:
            sorted_results = [
                (doc, score) for doc, score in sorted_results
                if score <= score_threshold
            ]

        # 8. 返回top_k文档
        final_docs = [doc for doc, _ in sorted_results[:top_k]]

        logger.info(
            f"[ExpandedSearch] 原始查询: '{query}', "
            f"扩展数: {len(expansions)}, "
            f"候选池: {candidate_pool}, "
            f"返回: {len(final_docs)} 条"
        )

        return final_docs


    def retrieve_with_strategy(
            self,
            query: str,
            strategy: str = "auto",
            **kwargs
    ) -> List[Document]:
        """
        按策略检索
        strategy: "base"基础, "mqe"多查询, "hyde"假设文档, "expanded"扩展, "auto"自动
        """
        if strategy == "base":
            return self.retriever.invoke(query)
        elif strategy == "mqe":
            return self.search_expanded(
                query,
                enable_mqe=True,
                enable_hyde=False,
                **kwargs
            )
        elif strategy == "hyde":
            return self.search_expanded(
                query,
                enable_mqe=False,
                enable_hyde=True,
                **kwargs
            )
        elif strategy == "expanded":
            return self.search_expanded(
                query,
                enable_mqe=True,
                enable_hyde=True,
                **kwargs
            )
        else:  # auto
            # 简单规则：长查询用HyDE，短查询用MQE
            if len(query) < 10:
                return self.search_expanded(
                    query,
                    enable_mqe=True,
                    enable_hyde=False,
                    **kwargs
                )
            else:
                return self.search_expanded(
                    query,
                    enable_mqe=True,
                    enable_hyde=True,
                    **kwargs
                )