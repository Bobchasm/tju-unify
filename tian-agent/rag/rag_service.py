import hashlib
from pathlib import Path
from typing import List, Dict, Optional, Any
from langchain_core.documents import Document
from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from rag.vectore_store import VectorStoreService
from rag.advanced_retrieval import AdvancedRetrieval
from utils.logger_handler import logger
from utils.config_handler import prompts_conf, retrieval_conf
from model.factory import chat_model
from utils.path_tools import get_abs_path


class RagSummarizeService:
    _PROMPT_TEXT: str = None

    def __init__(self, vector_store: Optional[VectorStoreService] = None):
        self.vector_store = vector_store or VectorStoreService()
        self.advanced_retrieval = AdvancedRetrieval(self.vector_store)
        self.retriever = self.vector_store.get_retriever()
        self.prompt_text = self._load_prompt_text()
        self.prompt_template = PromptTemplate.from_template(self.prompt_text)
        self.model = chat_model
        self.chain = self._init_chain()

        self.retrieval_config = retrieval_conf.get("retrieval", {})

    def _load_prompt_text(self) -> str:
        """加载提示词"""
        if self._PROMPT_TEXT is not None:
            return self._PROMPT_TEXT

        path = get_abs_path(prompts_conf["rag_summarize_prompt_path"])
        try:
            with open(path, "r", encoding="utf-8") as f:
                prompt_text = f.read().strip()
        except Exception as e:
            logger.error(f"读取提示词文件失败：{str(e)}")
            raise

        if not prompt_text:
            raise ValueError(f"提示词文件内容为空：{path}")

        self._PROMPT_TEXT = prompt_text
        return prompt_text

    def _policy_query_variants(self, query: str) -> List[str]:
        """职务/加分类问题：原文中「班长」在组织机构与加分表多处出现，单查询易召回错误片段。"""
        q = query.strip()
        out: List[str] = [q]
        triggers = (
            "班长", "团支书", "学委", "学生干部", "任职", "宿舍代表",
            "党支部", "学生会", "团委", "社团", "学生工作", "学生干部加分",
            "学生党团班",
        )
        if any(t in q for t in triggers):
            out.extend(
                [
                    "劳动技能素质 学生工作 测评依据 分值 班长 团支书",
                    "学生工作 基础分 班长 团支书 一学年 任期",
                ]
            )
        seen: set[str] = set()
        uniq: List[str] = []
        for x in out:
            if x not in seen:
                seen.add(x)
                uniq.append(x)
        return uniq

    def _retrieve_merged_for_summarize(
        self,
        query: str,
        strategy: str,
        history: Optional[List[Dict]],
        **kwargs,
    ) -> List[Document]:
        variants = self._policy_query_variants(query)
        top_k = int(self.retrieval_config.get("base", {}).get("top_k", 8))
        max_docs = min(top_k * 2, 16)

        merged: List[Document] = []
        seen_hashes: set[str] = set()

        for v in variants:
            batch = self.retrieve_docs(v, strategy, history, **kwargs)
            for d in batch:
                h = hashlib.md5(
                    d.page_content.encode("utf-8", errors="ignore")
                ).hexdigest()
                if h in seen_hashes:
                    continue
                seen_hashes.add(h)
                merged.append(d)
                if len(merged) >= max_docs:
                    return merged
        return merged

    def _init_chain(self):
        """初始化处理链"""
        return self.prompt_template | self.model | StrOutputParser()

    def retrieve_docs(
            self,
            query: str,
            strategy: str = "auto",
            history: Optional[List[Dict]] = None,
            **kwargs
    ) -> List[Document]:
        """
        检索文档，支持多种策略
        :param query: 查询
        :param strategy: 检索策略(base/mqe/hyde/expanded/auto)
        :param history: 对话历史
        :param kwargs: 其他参数
        """
        search_kwargs = {
            "top_k": self.retrieval_config.get("base", {}).get("top_k", 8),
            "score_threshold": self.retrieval_config.get("base", {}).get("score_threshold"),
            "enable_mqe": self.retrieval_config.get("mqe", {}).get("enabled", True),
            "mqe_expansions": self.retrieval_config.get("mqe", {}).get("expansions", 3),
            "enable_hyde": self.retrieval_config.get("hyde", {}).get("enabled", True),
            "candidate_pool_multiplier": self.retrieval_config.get("expanded", {}).get("candidate_pool_multiplier", 4),
        }
        search_kwargs.update(kwargs)

        if strategy == "base":
            return self.retriever.invoke(query)
        elif strategy in ["mqe", "hyde", "expanded", "auto"]:
            return self.advanced_retrieval.search_expanded(
                query=query,
                history=history,
                **search_kwargs
            )
        else:
            return self.advanced_retrieval.retrieve_with_strategy(
                query=query,
                strategy=strategy,
                history=history,
                **search_kwargs
            )

    def rag_summarize(
            self,
            query: str,
            strategy: str = "auto",
            history: Optional[List[Dict]] = None,
            **kwargs
    ) -> str:
        """
        执行RAG总结
        :param query: 用户查询
        :param strategy: 检索策略
        :param history: 对话历史
        :return: 总结结果
        """
        context_docs = self._retrieve_merged_for_summarize(
            query, strategy, history, **kwargs
        )

        if not context_docs:
            return (
                "未在已入库资料中检索到与问题直接相关的原文。"
                "请尝试更换关键词，或确认已将天津大学相关文档导入知识库并重新建库。"
            )

        context = ""
        for i, doc in enumerate(context_docs, 1):
            src = doc.metadata.get("source") or doc.metadata.get("file_path") or "未知来源"
            if isinstance(src, str):
                src = Path(src).name
            context += f"【参考资料{i}】文件名：{src}\n{doc.page_content}\n\n"

        input_dict = {
            "input": query,
            "context": context
        }

        result = self.chain.invoke(input_dict)

        logger.info(
            f"[RAG] 查询: '{query[:50]}...', "
            f"策略: {strategy}, "
            f"检索到: {len(context_docs)} 篇文档"
        )

        return result