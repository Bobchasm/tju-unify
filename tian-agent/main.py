import sys
import uuid
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))

import json

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
from agent.react_agent import ReactAgent
from utils.config_handler import agent_conf
from utils.conversation_summary_store import load_summary, save_summary
from utils import chat_history_store
from utils.unify_api_context import unify_api_context

app = FastAPI(title="小智 · 天津大学校园生活助手 API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

agent_instance = ReactAgent()

class Message(BaseModel):
    role: str = Field(..., description="消息角色，可选值: user, assistant")
    content: str = Field(..., description="消息内容")

class ChatRequest(BaseModel):
    session_id: Optional[str] = Field(None, description="会话 ID，如果不传则自动生成")
    messages: List[Message] = Field(..., description="历史消息列表")
    bearer_token: Optional[str] = Field(
        None,
        description="与前端一致的 JWT（可选），用于代登录用户调用 /unify-api 校园接口",
    )

class ChatResponse(BaseModel):
    session_id: str = Field(..., description="会话 ID")
    response: str = Field(..., description="响应内容")

sessions: Dict[str, Dict[str, Any]] = {}


def _chat_history_dir() -> str:
    return str((agent_conf or {}).get("chat_history_store_dir", "data/chat_history"))


@app.get("/")
async def root():
    return {"message": "小智 · 天津大学校园生活助手 API", "status": "running"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

@app.post("/api/chat")
async def chat(request: ChatRequest) -> ChatResponse:
    try:
        session_id = request.session_id or str(uuid.uuid4())
        
        if session_id not in sessions:
            sessions[session_id] = {
                "conversation_summary": "",
                "persist_enabled": bool((agent_conf or {}).get("conversation_summary_persist_enabled", True)),
                "store_dir": (agent_conf or {}).get("conversation_summary_store_dir", "data/conversation_memory")
            }
            
            if sessions[session_id]["persist_enabled"]:
                stored_summary = load_summary(sessions[session_id]["store_dir"], session_id)
                sessions[session_id]["conversation_summary"] = stored_summary
        
        messages = [{"role": msg.role, "content": msg.content} for msg in request.messages]
        
        agent_messages, updated_summary = agent_instance.build_agent_input(
            messages=messages,
            conversation_summary=sessions[session_id]["conversation_summary"]
        )
        
        sessions[session_id]["conversation_summary"] = updated_summary
        
        if sessions[session_id]["persist_enabled"]:
            save_summary(sessions[session_id]["store_dir"], session_id, updated_summary)
        
        full_response = ""
        with unify_api_context(request.bearer_token):
            for chunk in agent_instance.execute_stream(agent_messages=agent_messages):
                full_response += chunk

        hist = list(messages) + [{"role": "assistant", "content": full_response}]
        try:
            chat_history_store.save_messages(_chat_history_dir(), session_id, hist)
        except Exception:
            pass

        return ChatResponse(
            session_id=session_id,
            response=full_response
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/chat/stream")
async def chat_stream(request: ChatRequest):
    try:
        session_id = request.session_id or str(uuid.uuid4())
        
        if session_id not in sessions:
            sessions[session_id] = {
                "conversation_summary": "",
                "persist_enabled": bool((agent_conf or {}).get("conversation_summary_persist_enabled", True)),
                "store_dir": (agent_conf or {}).get("conversation_summary_store_dir", "data/conversation_memory")
            }
            
            if sessions[session_id]["persist_enabled"]:
                stored_summary = load_summary(sessions[session_id]["store_dir"], session_id)
                sessions[session_id]["conversation_summary"] = stored_summary
        
        messages = [{"role": msg.role, "content": msg.content} for msg in request.messages]
        
        agent_messages, updated_summary = agent_instance.build_agent_input(
            messages=messages,
            conversation_summary=sessions[session_id]["conversation_summary"]
        )
        
        sessions[session_id]["conversation_summary"] = updated_summary
        
        if sessions[session_id]["persist_enabled"]:
            save_summary(sessions[session_id]["store_dir"], session_id, updated_summary)
        
        messages_snapshot = [dict(m) for m in messages]

        async def generate():
            yield "data: " + json.dumps(
                {"event": "session", "session_id": session_id}, ensure_ascii=False
            ) + "\n\n"
            parts: List[str] = []
            with unify_api_context(request.bearer_token):
                for chunk in agent_instance.execute_stream(agent_messages=agent_messages):
                    parts.append(chunk)
                    yield "data: " + json.dumps(chunk, ensure_ascii=False) + "\n\n"
            assistant_full = "".join(parts)
            hist = messages_snapshot + [{"role": "assistant", "content": assistant_full}]
            try:
                chat_history_store.save_messages(_chat_history_dir(), session_id, hist)
            except Exception:
                pass
            yield f"data: [DONE]\n\n"
        
        return StreamingResponse(
            generate(), media_type="text/event-stream")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/chat/sessions")
async def list_chat_sessions(limit: int = 50):
    """列出本机 tian-agent 已持久化的会话（按最近更新时间倒序）。"""
    lim = limit if 1 <= limit <= 200 else 50
    sessions = chat_history_store.list_sessions(_chat_history_dir(), limit=lim)
    return {"sessions": sessions}


@app.get("/api/chat/history")
async def get_chat_history(session_id: str = ""):
    """按 session_id 拉取已持久化的完整对话（换浏览器/清缓存后仍可恢复）。"""
    sid = (session_id or "").strip()
    if not sid:
        return {"session_id": "", "messages": []}
    msgs = chat_history_store.load_messages(_chat_history_dir(), sid)
    return {"session_id": sid, "messages": msgs}


@app.delete("/api/chat/history")
async def delete_chat_history(session_id: str = ""):
    """删除某会话的完整对话记录文件。"""
    sid = (session_id or "").strip()
    if not sid:
        return {"ok": False, "detail": "session_id required"}
    chat_history_store.delete_messages(_chat_history_dir(), sid)
    return {"ok": True, "session_id": sid}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
