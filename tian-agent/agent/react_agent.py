import sys
from pathlib import Path

if __name__ == "__main__":
    _root = Path(__file__).resolve().parent.parent
    if str(_root) not in sys.path:
        sys.path.insert(0, str(_root))

from typing import Any, Dict, List, Optional, Sequence, Tuple

from langchain.agents import create_agent
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
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
)
from model.factory import chat_model
from utils.prompt_loader import load_conversation_summary_prompt, load_system_prompt
from utils.config_handler import agent_conf
from utils.conversation_memory import build_memory_window

"""
    用户输入 → 2. Agent处理 → 3. 工具调用 → 4. 响应生成

"""
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
            ],
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

        last_sent = ""

        for chunk in self.agent.stream(
            input_dict, stream_mode="values", context={"report": False}
        ):
            latest_message = chunk["messages"][-1]
            # 不向用户流式透出 ToolMessage 等；仅输出模型最终自然语言，避免误传大段 JSON。
            if not isinstance(latest_message, AIMessage):
                continue
            text = _text_from_ai_message(latest_message).strip()
            if not text:
                continue
            # stream_mode=values 可能对同一轮次重复推送同一条 AIMessage，去重避免前端重复的整段文本。
            if text == last_sent:
                continue
            last_sent = text
            yield text + "\n"

if __name__ == '__main__':
    agent = ReactAgent()
    for chunk in agent.execute_stream(query="北洋园校区和卫津路校区各有什么特点？"):
        print(chunk, end="", flush=True)
