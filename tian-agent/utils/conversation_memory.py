"""对话记忆：10轮滑动窗口 + 历史摘要压缩。"""

from __future__ import annotations

from typing import Any, Callable, Dict, List, MutableMapping, Sequence, Tuple


Message = MutableMapping[str, Any]
Summarizer = Callable[[str, Sequence[Dict[str, str]]], str]


def normalize_agent_messages(messages: Sequence[Message]) -> List[Dict[str, str]]:
    """仅保留 user/assistant 的纯文本消息。"""
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
    """
    将会话拆成完整轮次（user + 后续 assistant...）和尾部未完成消息。
    适合 chat UI 在“用户刚发问、助手尚未回复”时做记忆裁剪。
    """
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


def format_rounds_for_summary(rounds: Sequence[Sequence[Dict[str, str]]]) -> List[Dict[str, str]]:
    return flatten_rounds(rounds)


def build_summary_message(summary: str) -> Dict[str, str]:
    return {
        "role": "assistant",
        "content": (
            "以下为较早对话的摘要记忆，请仅将其作为历史上下文参考，"
            "如与检索结果或当前用户明确表述冲突，应以最新信息为准：\n"
            f"{summary.strip()}"
        ),
    }


def build_memory_window(
    messages: Sequence[Message],
    current_summary: str = "",
    max_rounds: int = 10,
    summarizer: Summarizer | None = None,
) -> Tuple[List[Dict[str, str]], str]:
    """
    返回传给模型的消息窗口，以及更新后的历史摘要。

    规则：
    1. 保留最近 max_rounds 个完整轮次。
    2. 更早的完整轮次压缩进摘要。
    3. 若当前最后一条是尚未得到回复的 user 消息，也一并保留。
    """
    normalized = normalize_agent_messages(messages)
    if not normalized:
        return [], current_summary.strip()

    rounds, pending_messages = split_conversation_rounds(normalized)
    if max_rounds <= 0:
        recent_rounds = rounds
        overflow_rounds: List[List[Dict[str, str]]] = []
    else:
        recent_rounds = rounds[-max_rounds:]
        overflow_rounds = rounds[:-max_rounds]

    updated_summary = current_summary.strip()
    if overflow_rounds and summarizer is not None:
        summary_input = format_rounds_for_summary(overflow_rounds)
        updated_summary = summarizer(updated_summary, summary_input).strip()

    window_messages = flatten_rounds(recent_rounds) + list(pending_messages)
    if updated_summary:
        window_messages = [build_summary_message(updated_summary)] + window_messages

    return window_messages, updated_summary
