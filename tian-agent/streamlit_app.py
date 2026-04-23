import time
import sys
import uuid
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
import streamlit as st
from agent.react_agent import ReactAgent
from utils.config_handler import agent_conf
from utils.conversation_summary_store import load_summary, save_summary

st.title("小智 · 天津大学校园生活助手")
st.divider()

if "message" not in st.session_state:
    st.session_state["message"] = [{"role": "assistant", "content": "你好，我是小智，天津大学校园生活助手。我可以结合学校公开资料，为你解答校园办事、校史校情、学习生活等常见问题。请问今天想了解什么？"}]

if "agent" not in st.session_state:
    st.session_state["agent"] = ReactAgent()

if "session_id" not in st.session_state:
    st.session_state["session_id"] = str(uuid.uuid4())

if "conversation_summary" not in st.session_state:
    persist_enabled = bool((agent_conf or {}).get("conversation_summary_persist_enabled", True))
    store_dir = (agent_conf or {}).get("conversation_summary_store_dir", "data/conversation_memory")
    st.session_state["conversation_summary"] = (
        load_summary(store_dir, st.session_state["session_id"]) if persist_enabled else ""
    )

for message in st.session_state["message"]:
    st.chat_message(message["role"]).write(message["content"])

prompt = st.chat_input()

if prompt:
    st.chat_message("user").write(prompt)
    st.session_state["message"].append({"role": "user", "content": prompt})

    agent_messages, updated_summary = st.session_state["agent"].build_agent_input(
        messages=st.session_state["message"],
        conversation_summary=st.session_state["conversation_summary"],
    )
    st.session_state["conversation_summary"] = updated_summary
    persist_enabled = bool((agent_conf or {}).get("conversation_summary_persist_enabled", True))
    if persist_enabled:
        store_dir = (agent_conf or {}).get("conversation_summary_store_dir", "data/conversation_memory")
        save_summary(store_dir, st.session_state["session_id"], updated_summary)

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
        st.rerun()
