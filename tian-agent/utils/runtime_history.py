from __future__ import annotations

from contextvars import ContextVar
from typing import Dict, List, Sequence


_history_var: ContextVar[List[Dict[str, str]]] = ContextVar("agent_history", default=[])


def set_history(messages: Sequence[Dict[str, str]]) -> None:
    _history_var.set(list(messages))


def get_history() -> List[Dict[str, str]]:
    return list(_history_var.get() or [])

