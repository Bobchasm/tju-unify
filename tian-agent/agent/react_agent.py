import sys
from pathlib import Path

if __name__ == "__main__":
    _root = Path(__file__).resolve().parent.parent
    if str(_root) not in sys.path:
        sys.path.insert(0, str(_root))

from typing import Any, Dict, List, Optional, Sequence, Tuple

from langchain.agents import create_agent
from langchain_core.messages import HumanMessage, SystemMessage
from agent.tools.middleware import monitor_tool, log_before_model, report_prompt_switch
from agent.tools.agent_tools import (rag_summarize,
                               fetch_student_data, fill_context_for_report,
                               get_user_id, get_current_semester, calculate_score)
from model.factory import chat_model
from utils.prompt_loader import load_conversation_summary_prompt, load_system_prompt
from utils.config_handler import agent_conf
from utils.conversation_memory import build_memory_window

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
        try:
            raw_rounds = (agent_conf or {}).get("conversation_window_rounds", 10)
            self._conversation_window_rounds = int(raw_rounds) if raw_rounds is not None else 10
        except (TypeError, ValueError):
            self._conversation_window_rounds = 10
        self._conversation_summary_enabled = bool(
            (agent_conf or {}).get("conversation_summary_enabled", True)
        )
        self._summary_prompt = load_conversation_summary_prompt()

    def _summarize_messages(
        self,
        current_summary: str,
        messages_to_compress: Sequence[Dict[str, str]],
    ) -> str:
        """将超出窗口的旧轮次压缩为短摘要。"""
        if not messages_to_compress:
            return current_summary.strip()

        history_text = "\n".join(
            f"{message['role']}: {message['content']}" for message in messages_to_compress
        )
        prompt = (
            f"{self._summary_prompt}\n\n"
            f"已有摘要：\n{current_summary.strip() or '（无）'}\n\n"
            f"新增历史对话：\n{history_text}"
        )
        response = chat_model.invoke(
            [
                SystemMessage(content="你负责维护多轮对话的长期记忆摘要。"),
                HumanMessage(content=prompt),
            ]
        )
        return (getattr(response, "content", "") or current_summary or "").strip()

    def build_agent_input(
        self,
        messages: List[Dict[str, Any]],
        conversation_summary: str = "",
    ) -> Tuple[List[Dict[str, str]], str]:
        summarizer = self._summarize_messages if self._conversation_summary_enabled else None
        return build_memory_window(
            messages=messages,
            current_summary=conversation_summary,
            max_rounds=self._conversation_window_rounds,
            summarizer=summarizer,
        )

    def execute_stream(
        self,
        agent_messages: Optional[List[Dict[str, str]]] = None,
        messages: Optional[List[Dict[str, Any]]] = None,
        query: Optional[str] = None,
        conversation_summary: str = "",
    ):
        """
        流式执行。优先使用已预处理的 agent_messages；
        其次使用 messages + conversation_summary 自动构造上下文；
        若仅传 query 则与旧版兼容，无历史。
        """
        if agent_messages is not None:
            prepared_messages = agent_messages
        elif messages is not None:
            prepared_messages, _ = self.build_agent_input(messages, conversation_summary)
        elif query is not None:
            prepared_messages = [{"role": "user", "content": query}]
        else:
            prepared_messages = []

        input_dict = {"messages": prepared_messages}

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
