import time
import sys
import os
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
import streamlit as st
from agent.react_agent import ReactAgent

st.title("小智 · 天津大学校园生活助手")
st.divider()

if "message" not in st.session_state:  # 防止一直创建
    st.session_state["message"] = [{"role": "assistant", "content": "你好，我是小智，天津大学校园生活助手。我可以结合学校公开资料，为你解答校园办事、校史校情、学习生活等常见问题。请问今天想了解什么？"}]

if "agent" not in st.session_state:
    st.session_state["agent"] = ReactAgent()

for message in st.session_state["message"]:
    st.chat_message(message["role"]).write(message["content"])

# 在页面最下方提供用户输入栏
prompt = st.chat_input()

if prompt:
    # 在页面输出用户的提问
    st.chat_message("user").write(prompt)
    st.session_state["message"].append({"role": "user", "content": prompt})

    response_messages = []
    with st.spinner("小智正在思考…"):
        res_stream = st.session_state["agent"].execute_stream(
            messages=st.session_state["message"]
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
