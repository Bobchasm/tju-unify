"""按 session_id 持久化完整对话列表（与摘要记忆分离）。"""

from __future__ import annotations

import json
import os
from datetime import datetime, timezone
from typing import Any, Dict, List

from utils.path_tools import get_abs_path


def _safe_filename(session_id: str) -> str:
    keep = []
    for ch in (session_id or "").strip():
        if ch.isalnum() or ch in ("-", "_"):
            keep.append(ch)
    return "".join(keep) or "default"


def _ensure_dir(dir_path: str) -> None:
    os.makedirs(dir_path, exist_ok=True)


def save_messages(store_dir: str, session_id: str, messages: List[Dict[str, Any]]) -> None:
    """保存完整消息列表，最多保留最后 300 条。"""
    base_dir = get_abs_path(store_dir)
    _ensure_dir(base_dir)
    path = os.path.join(base_dir, f"{_safe_filename(session_id)}.json")
    slim: List[Dict[str, str]] = []
    for m in messages:
        if not isinstance(m, dict):
            continue
        role = m.get("role")
        content = m.get("content")
        if role not in ("user", "assistant") or not isinstance(content, str):
            continue
        slim.append({"role": role, "content": content})
    if len(slim) > 300:
        slim = slim[-300:]
    payload = {"session_id": session_id, "messages": slim}
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)


def load_messages(store_dir: str, session_id: str) -> List[Dict[str, str]]:
    base_dir = get_abs_path(store_dir)
    path = os.path.join(base_dir, f"{_safe_filename(session_id)}.json")
    if not os.path.exists(path):
        return []
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        raw = data.get("messages")
        if not isinstance(raw, list):
            return []
        out: List[Dict[str, str]] = []
        for m in raw:
            if isinstance(m, dict) and m.get("role") in ("user", "assistant") and isinstance(
                m.get("content"), str
            ):
                out.append({"role": m["role"], "content": m["content"]})
        return out
    except Exception:
        return []


def _preview_from_messages(msgs: Any) -> str:
    if not isinstance(msgs, list):
        return ""
    for m in reversed(msgs):
        if not isinstance(m, dict):
            continue
        role = m.get("role")
        content = m.get("content")
        if role not in ("user", "assistant") or not isinstance(content, str):
            continue
        t = content.strip().replace("\n", " ")
        if not t:
            continue
        return (t[:80] + "…") if len(t) > 80 else t
    return ""


def list_sessions(store_dir: str, limit: int = 50) -> List[Dict[str, Any]]:
    """扫描存档目录，按文件修改时间倒序列出会话摘要（供前端「历史对话」列表）。"""
    base_dir = get_abs_path(store_dir)
    if not os.path.isdir(base_dir):
        return []
    rows: List[Dict[str, Any]] = []
    for name in os.listdir(base_dir):
        if not name.endswith(".json"):
            continue
        path = os.path.join(base_dir, name)
        if not os.path.isfile(path):
            continue
        try:
            mtime = os.path.getmtime(path)
        except OSError:
            continue
        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception:
            continue
        sid = data.get("session_id")
        if not isinstance(sid, str) or not sid.strip():
            sid = name[:-5]
        raw_msgs = data.get("messages")
        cnt = len(raw_msgs) if isinstance(raw_msgs, list) else 0
        preview = _preview_from_messages(raw_msgs)
        rows.append(
            {
                "session_id": sid.strip(),
                "message_count": cnt,
                "preview": preview,
                "updated_at": int(mtime * 1000),
                "updated_at_iso": datetime.fromtimestamp(mtime, tz=timezone.utc).isoformat(),
            }
        )
    rows.sort(key=lambda r: r["updated_at"], reverse=True)
    cap = min(max(limit, 1), 200)
    return rows[:cap]


def delete_messages(store_dir: str, session_id: str) -> bool:
    base_dir = get_abs_path(store_dir)
    path = os.path.join(base_dir, f"{_safe_filename(session_id)}.json")
    if os.path.exists(path):
        try:
            os.remove(path)
            return True
        except OSError:
            return False
    return False
