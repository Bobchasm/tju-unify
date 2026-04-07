import sys
from pathlib import Path

if __name__ == "__main__":
    _root = Path(__file__).resolve().parent.parent
    if str(_root) not in sys.path:
        sys.path.insert(0, str(_root))

from typing import Any, Dict, List, Optional

from langchain.agents import create_agent
from agent.tools.middleware import monitor_tool, log_before_model, report_prompt_switch
from agent.tools.agent_tools import (rag_summarize,
                               fetch_student_data, fill_context_for_report,
                               get_user_id, get_current_semester, calculate_score)
from model.factory import chat_model
from utils.prompt_loader import load_system_prompt
from utils.config_handler import agent_conf
from utils.conversation_memory import trim_message_window

"""
    用户输入 → 2. Agent处理 → 3. 工具调用 → 4. 响应生成

"""
class ReactAgent(object):
    def __init__(self):
        self.agent = create_agent(
            model=chat_model,
            system_prompt=load_system_prompt(),
            tools=[rag_summarize, get_user_id, get_current_semester,
                   fetch_student_data, calculate_score, fill_context_for_report],
            middleware=[monitor_tool, log_before_model, report_prompt_switch],
        )
        raw = (agent_conf or {}).get("conversation_max_messages", 24)
        try:
            self._conversation_max_messages = int(raw) if raw is not None else 24
        except (TypeError, ValueError):
            self._conversation_max_messages = 24

    def _to_agent_messages(self, messages: List[Dict[str, Any]]) -> List[Dict[str, str]]:
        """仅保留 user/assistant 文本轮次，供 create_agent 使用。"""
        out: List[Dict[str, str]] = []
        for m in messages:
            role = m.get("role")
            content = m.get("content")
            if role not in ("user", "assistant") or not content:
                continue
            if isinstance(content, str):
                out.append({"role": role, "content": content})
        return out

    def execute_stream(
        self,
        messages: Optional[List[Dict[str, Any]]] = None,
        query: Optional[str] = None,
    ):
        """
        流式执行。优先使用 messages（含历史）；若仅传 query 则与旧版兼容，无历史。
        messages 建议为完整会话列表，含当前用户本轮输入；内部按窗口截断。
        """
        if messages is not None:
            agent_messages = self._to_agent_messages(messages)
        elif query is not None:
            agent_messages = [{"role": "user", "content": query}]
        else:
            agent_messages = []

        windowed = trim_message_window(
            agent_messages,
            self._conversation_max_messages,
        )
        input_dict = {"messages": windowed}

        for chunk in self.agent.stream(
            input_dict, stream_mode="values", context={"report": False}
        ):
            latest_message = chunk["messages"][-1]
            text = getattr(latest_message, "content", None) or ""
            if text:
                yield text.strip() + "\n"

if __name__ == '__main__':
    agent = ReactAgent()
    for chunk in agent.execute_stream(query="北洋园校区和卫津路校区各有什么特点？"):
        print(chunk, end="", flush=True)
