import time
import sys
import uuid
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
import streamlit as st
from agent.react_agent import ReactAgent
from utils.config_handler import agent_conf
from utils.agentic_memory import AgenticMemoryService

st.title("小智 · 天津大学校园生活助手")
st.divider()

if "message" not in st.session_state:
    st.session_state["message"] = [{"role": "assistant", "content": "你好，我是小智，天津大学校园生活助手。我可以结合学校公开资料，为你解答校园办事、校史校情、学习生活等常见问题。请问今天想了解什么？"}]

if "agent" not in st.session_state:
    st.session_state["agent"] = ReactAgent()

if "session_id" not in st.session_state:
    st.session_state["session_id"] = str(uuid.uuid4())

_agentic_on = bool((agent_conf or {}).get("agentic_memory_enabled", True))
if _agentic_on and "agentic" not in st.session_state:
    st.session_state["agentic"] = AgenticMemoryService(st.session_state["session_id"])

for message in st.session_state["message"]:
    st.chat_message(message["role"]).write(message["content"])

prompt = st.chat_input()

if prompt:
    st.chat_message("user").write(prompt)
    st.session_state["message"].append({"role": "user", "content": prompt})

    mem = st.session_state.get("agentic") if _agentic_on else None
    agent_messages = st.session_state["agent"].build_agent_input(
        messages=st.session_state["message"],
        memory=mem,
    )

    response_messages = []
    with st.spinner("小智正在思考…"):
        res_stream = st.session_state["agent"].execute_stream(
            agent_messages=agent_messages
        )

        def capture(generator, cache_list):
            for chunk in generator:
                cache_list.append(chunk)
                for char in chunk:
                    time.sleep(0.01)
                    yield char

        st.chat_message("assistant").write_stream(capture(res_stream, response_messages))
        full_reply = "".join(response_messages)
        st.session_state["message"].append({"role": "assistant", "content": full_reply})
        if mem is not None:
            mem.add_round(prompt, full_reply)
        st.rerun()
