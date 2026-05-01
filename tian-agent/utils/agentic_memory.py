# 笔记、向量链接、记忆演进、检索+链接扩展。

from __future__ import annotations

import json
import re
import uuid
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional, Set, Tuple

from langchain_chroma import Chroma
from langchain_core.messages import HumanMessage, SystemMessage

from model.factory import chat_model, embed_model
from utils.config_handler import agent_conf
from utils.conversation_memory import (
    normalize_agent_messages,
    split_conversation_rounds,
    flatten_rounds,
)
from utils.logger_handler import logger
from utils.path_tools import get_abs_path
from utils.prompt_loader import load_agentic_evolve_prompt, load_agentic_note_prompt


@dataclass
class MemoryNote:
    id: str
    text: str
    ts: str
    context: str
    keywords: List[str] = field(default_factory=list)
    tags: List[str] = field(default_factory=list)
    links: List[str] = field(default_factory=list)

    def embedding_text(self) -> str:
        kw = " ".join(self.keywords)
        tg = " ".join(self.tags)
        return f"{self.context}\n{kw}\n{tg}\n{self.text}".strip()

    def to_dict(self) -> Dict[str, Any]:
        d = asdict(self)
        return d

    @staticmethod
    def from_dict(d: Dict[str, Any]) -> "MemoryNote":
        return MemoryNote(
            id=str(d.get("id", "")),
            text=str(d.get("text", "")),
            ts=str(d.get("ts", "")),
            context=str(d.get("context", "")),
            keywords=list(d.get("keywords") or []),
            tags=list(d.get("tags") or []),
            links=[str(x) for x in (d.get("links") or [])],
        )


def _safe_session_dir(session_id: str) -> str:
    keep: List[str] = []
    for ch in (session_id or "").strip():
        if ch.isalnum() or ch in ("-", "_"):
            keep.append(ch)
    return "".join(keep) or "default"


def _parse_json_object(text: str) -> Dict[str, Any]:
    t = (text or "").strip()
    m = re.search(r"\{[\s\S]*\}", t)
    if not m:
        return {}
    try:
        return json.loads(m.group(0))
    except json.JSONDecodeError:
        return {}


def _chroma_get_config() -> Tuple[str, str, str]:
    c = agent_conf or {}
    persist = c.get("agentic_chroma_persist_dir", "data/agentic_chroma")
    name = c.get("agentic_chroma_collection", "episodic_mem")
    return get_abs_path(persist), str(name), persist


_vector_store: Optional[Chroma] = None


def get_episodic_vector_store() -> Chroma:
    global _vector_store
    if _vector_store is None:
        persist, collection_name, _ = _chroma_get_config()
        _vector_store = Chroma(
            collection_name=collection_name,
            embedding_function=embed_model,
            persist_directory=persist,
        )
    return _vector_store


def get_recent_only_messages(messages: List[Dict[str, Any]], max_rounds: int) -> List[Dict[str, str]]:
    #无 Agentic 记忆时：仅保留最近完整轮 + 未闭合 user。规则同原 sliding window 去摘要部分。
    normalized = normalize_agent_messages(messages)
    if not normalized:
        return []
    rounds, pending = split_conversation_rounds(normalized)
    if max_rounds <= 0:
        recent_rounds = rounds
    else:
        recent_rounds = rounds[-max_rounds:]
    return flatten_rounds(recent_rounds) + list(pending)


class AgenticMemoryService:
    #单会话的笔记 store：JSON 为源、Chroma 为向量索引。

    def __init__(self, session_id: str) -> None:
        self.session_id = session_id
        c = agent_conf or {}
        self._store_dir = get_abs_path(c.get("agentic_memory_persist_dir", "data/agentic_memory"))
        self._link_k = int(c.get("agentic_link_k", 5) or 5)
        self._retrieve_k = int(c.get("agentic_retrieve_k", 8) or 8)
        self._max_expand = int(c.get("agentic_max_expand_total", 20) or 20)
        self._evolution_neighbor_k = int(c.get("agentic_evolution_neighbor_k", 5) or 5)
        self.notes: Dict[str, MemoryNote] = {}
        self._path = _session_notes_path(self._store_dir, session_id)
        self._load_json()
        self._sync_chroma_on_load()

    def _filter_kwargs(self) -> Optional[Dict[str, Any]]:
        return {"session_id": self.session_id}

    def _load_json(self) -> None:
        import os

        if not os.path.exists(self._path):
            return
        try:
            with open(self._path, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            logger.warning(f"[agentic_memory] 读取 {self._path} 失败: {e}")
            return
        raw = data.get("notes")
        if not isinstance(raw, list):
            return
        for item in raw:
            if not isinstance(item, dict):
                continue
            n = MemoryNote.from_dict(item)
            if n.id and n.text:
                self.notes[n.id] = n

    def _save_json(self) -> None:
        import os

        base = os.path.dirname(self._path)
        os.makedirs(base, exist_ok=True)
        payload = {
            "session_id": self.session_id,
            "notes": [n.to_dict() for n in self.notes.values()],
        }
        with open(self._path, "w", encoding="utf-8") as f:
            json.dump(payload, f, ensure_ascii=False, indent=2)

    def _sync_chroma_on_load(self) -> None:
        for nid, note in self.notes.items():
            self._chroma_upsert(note, nid)

    def _chroma_upsert(self, note: MemoryNote, note_id: str) -> None:
        try:
            store = get_episodic_vector_store()
            try:
                store.delete(ids=[note_id])
            except Exception:
                pass
            text = note.embedding_text()
            meta: Dict[str, Any] = {
                "session_id": self.session_id,
                "note_id": note_id,
            }
            store.add_texts(
                texts=[text],
                metadatas=[meta],
                ids=[note_id],
            )
        except Exception as e:
            logger.warning(f"[agentic_memory] Chroma upsert 失败: {e}")

    def _chroma_delete(self, note_id: str) -> None:
        try:
            get_episodic_vector_store().delete(ids=[note_id])
        except Exception as e:
            logger.warning(f"[agentic_memory] Chroma delete 失败: {e}")

    def _search_similar(self, query: str, k: int) -> List[Tuple[str, float]]:
        if not (query or "").strip() or not self.notes:
            return []
        try:
            store = get_episodic_vector_store()
        except Exception as e:
            logger.warning(f"[agentic_memory] Chroma 不可用: {e}")
            return []
        k_fetch = max(k * 4, 16, k)
        res_raw: List[Any] = []
        flt = self._filter_kwargs()
        for use_filter in (True, False) if flt is not None else (False,):
            try:
                if use_filter and flt is not None:
                    res_raw = store.similarity_search_with_relevance_scores(
                        query, k=k_fetch, filter=flt
                    )
                else:
                    res_raw = store.similarity_search_with_relevance_scores(
                        query, k=k_fetch
                    )
                break
            except TypeError:
                if use_filter and flt is not None:
                    continue
                res_raw = []
            except Exception as e:
                if use_filter and flt is not None:
                    continue
                logger.warning(f"[agentic_memory] 向量检索失败: {e}")
                res_raw = []
        if not res_raw and hasattr(store, "similarity_search"):
            try:
                docs = store.similarity_search(query, k=k_fetch, filter=flt) if flt else store.similarity_search(query, k=k_fetch)  # type: ignore[arg-type]
            except TypeError:
                docs = store.similarity_search(query, k=k_fetch)
            res_raw = [(d, 1.0) for d in docs]
        out: List[Tuple[str, float]] = []
        for item in res_raw:
            if len(item) < 2:
                continue
            doc, score = item[0], item[1]
            md = doc.metadata or {}
            nid = md.get("note_id")
            if not isinstance(nid, str) or nid not in self.notes:
                continue
            if md.get("session_id") and md.get("session_id") != self.session_id:
                continue
            out.append((nid, float(score)))
        out.sort(key=lambda x: -x[1])
        uniq: List[Tuple[str, float]] = []
        seen: Set[str] = set()
        for nid, s in out:
            if nid not in seen:
                seen.add(nid)
                uniq.append((nid, s))
        return uniq[:k]

    def retrieve_notes_for_query(self, query: str) -> List[MemoryNote]:
        if not (query or "").strip():
            return []
        pairs = self._search_similar(query.strip(), self._retrieve_k)
        ordered_ids: List[str] = [p[0] for p in pairs]
        seen: Set[str] = set(ordered_ids)
        expanded: List[MemoryNote] = []
        for nid in ordered_ids:
            n = self.notes.get(nid)
            if n:
                expanded.append(n)
        for n in list(expanded):
            for lid in n.links:
                if lid in seen or lid not in self.notes:
                    continue
                seen.add(lid)
                expanded.append(self.notes[lid])
        return expanded[: self._max_expand]

    def _format_memory_block(self, notes: List[MemoryNote]) -> str:
        lines: List[str] = []
        for i, n in enumerate(notes, 1):
            kw = "、".join(n.keywords) if n.keywords else "—"
            tg = "、".join(n.tags) if n.tags else "—"
            lk = "、".join(n.links) if n.links else "—"
            lines.append(
                f"[笔记{i}] 时间(UTC) {n.ts} | 关键词: {kw} | 标签: {tg} | 链接: {lk}\n"
                f"原文：{n.text}\n"
                f"上下文：{n.context or '—'}"
            )
        return "\n\n".join(lines)

    def _memory_styled_message(self, block: str) -> Dict[str, str]:
        return {
            "role": "assistant",
            "content": (
                "以下为从本会话「结构化长期记忆」中检索到的相关笔记，仅作对话连贯性参考；"
                "校园办事知识请依赖检索工具，备忘录/二手/跑腿等业务状态请以工具接口为准，勿与下述混为一谈。\n\n"
                f"{block}"
            ),
        }

    def build_model_messages(
        self,
        messages: List[Dict[str, Any]],
        recent_rounds: int,
    ) -> List[Dict[str, str]]:
        normalized = normalize_agent_messages(messages)
        if not normalized:
            return []
        tail = get_recent_only_messages(messages, recent_rounds)
        last_user = ""
        for m in reversed(normalized):
            if m.get("role") == "user":
                last_user = m.get("content", "").strip()
                break
        mem_notes = self.retrieve_notes_for_query(last_user) if self.notes else []
        if not mem_notes:
            return tail
        block = self._format_memory_block(mem_notes)
        if len(block) > 12000:
            block = block[:12000] + "\n…（已截断）"
        return [self._memory_styled_message(block)] + tail

    def _analyze_new_note(self, full_text: str) -> Tuple[str, List[str], List[str]]:
        prompt = load_agentic_note_prompt() + f"\n\n【对话】\n{full_text}\n"
        r = chat_model.invoke(
            [
                SystemMessage(content="你只输出一个 JSON 对象，不要其他文字。"),
                HumanMessage(content=prompt),
            ]
        )
        content = (getattr(r, "content", None) or "").strip()
        d = _parse_json_object(content)
        context = d.get("context", "")
        if not isinstance(context, str):
            context = str(context)
        kws = d.get("keywords", [])
        tgs = d.get("tags", [])
        if not isinstance(kws, list):
            kws = []
        if not isinstance(tgs, list):
            tgs = []
        kws = [str(x) for x in kws if str(x).strip()][:20]
        tgs = [str(x) for x in tgs if str(x).strip()][:20]
        return (context.strip() or "无额外语境", kws, tgs)

    def _neighbors_for_evolution(
        self, content_query: str, k: int
    ) -> Tuple[str, List[str]]:
        
        # 近邻只从当前 self.notes 中检索，不包含尚未写入的「新」笔记。
        
        lines: List[str] = []
        ids: List[str] = []
        for nid, _ in self._search_similar(content_query, k):
            n = self.notes.get(nid)
            if not n:
                continue
            part = (
                f"memory_id:{nid}\ttalk start time:{n.ts}\t"
                f"memory content: {n.text}\tmemory context: {n.context}\t"
                f"memory keywords: {str(n.keywords)}\tmemory tags: {str(n.tags)}\n"
            )
            lines.append(part)
            ids.append(nid)
        return "".join(lines), ids

    def _process_memory_amem(
        self, note: MemoryNote
    ) -> Tuple[bool, MemoryNote, Set[str]]:
        #
        #对齐 A-mem-sys `process_memory`：近邻 + 长式演进提示词，解析 JSON
        #执行 strengthen / update_neighbor。返回 (should_evolve, 可能已修改的 note, 被更新的近邻 id)。
        
        if not self.notes:
            return False, note, set()
        nbr_str, nbr_ids = self._neighbors_for_evolution(
            note.text, k=self._evolution_neighbor_k
        )
        if not nbr_str or not nbr_ids:
            return False, note, set()
        kw_display = str(note.keywords) if note.keywords else "[]"
        try:
            body = load_agentic_evolve_prompt().format(
                context=note.context,
                content=note.text,
                keywords=kw_display,
                nearest_neighbors_memories=nbr_str,
                neighbor_number=len(nbr_ids),
            )
        except KeyError as e:
            logger.warning(f"[agentic_memory] 演进提示词 format 失败: {e}")
            return False, note, set()
        try:
            r = chat_model.invoke(
                [
                    SystemMessage(
                        content="You must output only a single JSON object, no markdown, no other text."
                    ),
                    HumanMessage(content=body),
                ]
            )
        except Exception as e:
            logger.warning(f"[agentic_memory] 演进 LLM 调用失败: {e}")
            return False, note, set()
        raw = (getattr(r, "content", None) or "").strip()
        d = _parse_json_object(raw)
        if not d or not d.get("should_evolve"):
            return False, note, set()
        nbr_set = set(nbr_ids)
        updated_neighbors: Set[str] = set()
        actions = d.get("actions")
        if not isinstance(actions, list):
            actions = []
        for action in actions:
            if not isinstance(action, str):
                continue
            a = action.strip().lower()
            if a == "strengthen":
                for sid in d.get("suggested_connections") or []:
                    if not isinstance(sid, str) or sid not in nbr_set:
                        continue
                    if sid not in note.links:
                        note.links.append(sid)
                tu = d.get("tags_to_update")
                if isinstance(tu, list) and tu:
                    note.tags = [str(x) for x in tu if str(x).strip()][:30]
            elif a == "update_neighbor":
                nctx = d.get("new_context_neighborhood")
                ntgs = d.get("new_tags_neighborhood")
                if not isinstance(nctx, list):
                    nctx = []
                if not isinstance(ntgs, list):
                    ntgs = []
                for i, mid in enumerate(nbr_ids):
                    om = self.notes.get(mid)
                    if not om:
                        continue
                    new_ctx = om.context
                    if i < len(nctx) and isinstance(nctx[i], str) and nctx[i].strip():
                        new_ctx = nctx[i].strip()
                    new_tags = list(om.tags)
                    if i < len(ntgs) and isinstance(ntgs[i], list):
                        new_tags = [str(x) for x in ntgs[i] if str(x).strip()][:30]
                    self.notes[mid] = MemoryNote(
                        id=om.id,
                        text=om.text,
                        ts=om.ts,
                        context=new_ctx,
                        keywords=list(om.keywords),
                        tags=new_tags,
                        links=list(om.links),
                    )
                    updated_neighbors.add(mid)
        return True, note, updated_neighbors

    def add_round(self, user_text: str, assistant_text: str) -> None:
        user_text = (user_text or "").strip()
        assistant_text = (assistant_text or "").strip()
        if not user_text and not assistant_text:
            return
        full = f"user: {user_text}\nassistant: {assistant_text}"
        context, kws, tags = self._analyze_new_note(full)
        now = datetime.now(timezone.utc).isoformat()
        note_id = str(uuid.uuid4())
        new = MemoryNote(
            id=note_id,
            text=full,
            ts=now,
            context=context,
            keywords=kws,
            tags=tags,
            links=[],
        )
        if not self.notes:
            self.notes[note_id] = new
            self._chroma_upsert(new, note_id)
            self._save_json()
            return
        _, new, updated = self._process_memory_amem(new)
        self.notes[note_id] = new
        self._chroma_upsert(new, note_id)
        for uid in updated:
            u = self.notes.get(uid)
            if u:
                self._chroma_upsert(u, uid)
        self._save_json()


def _session_notes_path(store_dir: str, session_id: str) -> str:
    import os

    d = _safe_session_dir(session_id)
    return os.path.join(get_abs_path(store_dir), d, "notes.json")


def delete_episodic_session_data(session_id: str) -> None:
    #删除某 session 的本地笔记 JSON，并从 episodic Chroma 中移除对应 id。
    import os

    c = agent_conf or {}
    store_dir = c.get("agentic_memory_persist_dir", "data/agentic_memory")
    path = _session_notes_path(store_dir, session_id)
    ids: List[str] = []
    if os.path.exists(path):
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
            for it in data.get("notes") or []:
                if isinstance(it, dict) and it.get("id"):
                    ids.append(str(it["id"]))
        except Exception as e:
            logger.warning(f"[agentic_memory] 读取待删笔记失败: {e}")
    try:
        st = get_episodic_vector_store()
        for i in ids:
            try:
                st.delete(ids=[i])
            except Exception:
                pass
    except Exception as e:
        logger.warning(f"[agentic_memory] 删除 Chroma: {e}")
    try:
        if os.path.exists(path):
            os.remove(path)
        parent = os.path.dirname(path)
        if os.path.isdir(parent) and not os.listdir(parent):
            os.rmdir(parent)
    except OSError:
        pass
