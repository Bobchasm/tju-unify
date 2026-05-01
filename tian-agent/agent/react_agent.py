import sys
from pathlib import Path

if __name__ == "__main__":
    _root = Path(__file__).resolve().parent.parent
    if str(_root) not in sys.path:
        sys.path.insert(0, str(_root))

from typing import Any, Dict, List, Optional

from langchain.agents import create_agent
from langchain_core.messages import AIMessage
from agent.tools.middleware import monitor_tool, log_before_model, report_prompt_switch
from agent.tools.agent_tools import (rag_summarize,
                               fetch_student_data, fill_context_for_report,
                               get_user_id, get_current_semester, calculate_score)
from agent.tools.unify_campus_tools import (
    unify_get_school_news,
    unify_get_school_news_detail,
    unify_search_secondhand_posts,
    unify_list_secondhand_posts,
    unify_get_secondhand_post_detail,
    unify_submit_trade_request,
    unify_list_trade_requests_for_post,
    unify_memo_get_agent_snapshot,
    unify_memo_create_with_reminder,
    unify_memo_set_task_done,
)
from model.factory import chat_model
from utils.prompt_loader import load_system_prompt
from utils.config_handler import agent_conf
from utils.agentic_memory import AgenticMemoryService, get_recent_only_messages


def _text_from_ai_message(msg: AIMessage) -> str:
    """提取 AIMessage 的纯文本；兼容字符串与新版块列表结构。"""
    content = getattr(msg, "content", None)
    if content is None:
        return ""
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        parts: List[str] = []
        for block in content:
            if isinstance(block, str):
                parts.append(block)
            elif isinstance(block, dict):
                if block.get("type") == "text" and block.get("text"):
                    parts.append(str(block["text"]))
        return "".join(parts)
    return str(content)


class ReactAgent(object):
    def __init__(self):
        self.agent = create_agent(
            model=chat_model,
            system_prompt=load_system_prompt(),
            tools=[
                rag_summarize,
                get_user_id,
                get_current_semester,
                fetch_student_data,
                calculate_score,
                fill_context_for_report,
                unify_get_school_news,
                unify_get_school_news_detail,
                unify_search_secondhand_posts,
                unify_list_secondhand_posts,
                unify_get_secondhand_post_detail,
                unify_submit_trade_request,
                unify_list_trade_requests_for_post,
                unify_memo_get_agent_snapshot,
                unify_memo_create_with_reminder,
                unify_memo_set_task_done,
            ],
            middleware=[monitor_tool, log_before_model, report_prompt_switch],
        )
        self._agentic_enabled = bool((agent_conf or {}).get("agentic_memory_enabled", True))
        try:
            ar = (agent_conf or {}).get("agentic_recent_rounds")
            if ar is not None:
                self._recent_rounds = int(ar)
            else:
                raw = (agent_conf or {}).get("conversation_window_rounds", 4)
                self._recent_rounds = int(raw) if raw is not None else 4
        except (TypeError, ValueError):
            self._recent_rounds = 4

    def build_agent_input(
        self,
        messages: List[Dict[str, Any]],
        memory: Optional[AgenticMemoryService] = None,
    ) -> List[Dict[str, str]]:
        if self._agentic_enabled and memory is not None:
            return memory.build_model_messages(messages, self._recent_rounds)
        return get_recent_only_messages(messages, self._recent_rounds)

    def execute_stream(
        self,
        agent_messages: Optional[List[Dict[str, str]]] = None,
        messages: Optional[List[Dict[str, Any]]] = None,
        query: Optional[str] = None,
        memory: Optional[AgenticMemoryService] = None,
    ):
        """
        流式执行。优先使用已预处理的 agent_messages；
        其次使用 messages + 可选的 AgenticMemoryService 自动构造上下文；
        若仅传 query 则与旧版兼容，无历史。
        """
        if agent_messages is not None:
            prepared_messages = agent_messages
        elif messages is not None:
            prepared_messages = self.build_agent_input(messages, memory=memory)
        elif query is not None:
            prepared_messages = [{"role": "user", "content": query}]
        else:
            prepared_messages = []

        input_dict = {"messages": prepared_messages}

        last_sent = ""

        for chunk in self.agent.stream(
            input_dict, stream_mode="values", context={"report": False}
        ):
            latest_message = chunk["messages"][-1]
            if not isinstance(latest_message, AIMessage):
                continue
            text = _text_from_ai_message(latest_message).strip()
            if not text:
                continue
            if text == last_sent:
                continue
            last_sent = text
            yield text + "\n"
