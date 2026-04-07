"""对话窗口：限制传入模型的历史条数，避免超长上下文。"""

from __future__ import annotations

from typing import Any, List, MutableMapping


def trim_message_window(
    messages: List[MutableMapping[str, Any]],
    max_messages: int,
) -> List[MutableMapping[str, Any]]:
    """
    保留最近 max_messages 条对话（每条为 user 或 assistant 的一条记录）。
    max_messages <= 0 表示不截断。
    """
    if not messages:
        return []
    if max_messages <= 0 or len(messages) <= max_messages:
        return list(messages)
    return list(messages[-max_messages:])
