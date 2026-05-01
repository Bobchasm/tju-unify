#对话消息规范化与轮次切分。供 A-MEM 式对话记忆等模块复用。

from __future__ import annotations

from typing import Any, Dict, List, MutableMapping, Sequence, Tuple


Message = MutableMapping[str, Any]


def normalize_agent_messages(messages: Sequence[Message]) -> List[Dict[str, str]]:
    #仅保留 user/assistant 的纯文本消息。
    normalized: List[Dict[str, str]] = []
    for message in messages:
        role = message.get("role")
        content = message.get("content")
        if role not in ("user", "assistant") or not isinstance(content, str) or not content.strip():
            continue
        normalized.append({"role": role, "content": content.strip()})
    return normalized


def split_conversation_rounds(
    messages: Sequence[Dict[str, str]],
) -> Tuple[List[List[Dict[str, str]]], List[Dict[str, str]]]:
    
    # 将会话拆成完整轮次（user + 后续 assistant...）和尾部未完成消息。
    #适合 chat UI 在「用户刚发问、助手尚未回复」时做记忆裁剪。
    
    rounds: List[List[Dict[str, str]]] = []
    current_round: List[Dict[str, str]] = []

    for message in messages:
        role = message["role"]
        if role == "user":
            if current_round:
                rounds.append(current_round)
            current_round = [message]
            continue

        if current_round:
            current_round.append(message)
        else:
            rounds.append([message])

    pending_messages: List[Dict[str, str]] = []
    if current_round:
        if len(current_round) == 1 and current_round[0]["role"] == "user":
            pending_messages = current_round
        else:
            rounds.append(current_round)

    return rounds, pending_messages


def flatten_rounds(rounds: Sequence[Sequence[Dict[str, str]]]) -> List[Dict[str, str]]:
    flattened: List[Dict[str, str]] = []
    for one_round in rounds:
        flattened.extend(one_round)
    return flattened
