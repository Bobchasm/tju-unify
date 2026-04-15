from __future__ import annotations

import json
import os
from typing import Optional

from utils.path_tools import get_abs_path


def _safe_filename(session_id: str) -> str:
    keep = []
    for ch in session_id.strip():
        if ch.isalnum() or ch in ("-", "_"):
            keep.append(ch)
    return "".join(keep) or "default"


def _ensure_dir(dir_path: str) -> None:
    os.makedirs(dir_path, exist_ok=True)


def load_summary(store_dir: str, session_id: str) -> str:
    base_dir = get_abs_path(store_dir)
    _ensure_dir(base_dir)
    path = os.path.join(base_dir, f"{_safe_filename(session_id)}.json")
    if not os.path.exists(path):
        return ""
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        summary = data.get("summary", "")
        return summary if isinstance(summary, str) else ""
    except Exception:
        return ""


def save_summary(store_dir: str, session_id: str, summary: str) -> None:
    base_dir = get_abs_path(store_dir)
    _ensure_dir(base_dir)
    path = os.path.join(base_dir, f"{_safe_filename(session_id)}.json")
    payload = {"session_id": session_id, "summary": summary or ""}
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)

