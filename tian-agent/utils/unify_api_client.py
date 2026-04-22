"""调用与前端一致的网关路径：/unify-api/news、/unify-api/transaction。"""

from __future__ import annotations

import json
import os
from typing import Any, Dict, Optional

import httpx

from utils.config_handler import agent_conf
from utils.unify_api_context import get_unify_bearer_token


def get_unify_api_base_url() -> str:
    env = (os.environ.get("UNIFY_API_BASE_URL") or "").strip()
    if env:
        return env.rstrip("/")
    conf = (agent_conf or {}).get("unify_api_base_url")
    if isinstance(conf, str) and conf.strip():
        return conf.strip().rstrip("/")
    return "http://127.0.0.1:7070"


def _auth_headers() -> Dict[str, str]:
    headers: Dict[str, str] = {"Content-Type": "application/json"}
    raw = get_unify_bearer_token()
    if raw:
        tok = raw.strip()
        if tok and not tok.lower().startswith("bearer "):
            tok = f"Bearer {tok}"
        headers["Authorization"] = tok
    return headers


def unify_http_get(path: str, params: Optional[Dict[str, Any]] = None) -> str:
    return _request("GET", path, params=params)


def unify_http_post(path: str, json_body: Dict[str, Any]) -> str:
    return _request("POST", path, json_body=json_body)


def unify_http_put(
    path: str,
    json_body: Optional[Dict[str, Any]] = None,
    params: Optional[Dict[str, Any]] = None,
) -> str:
    return _request("PUT", path, params=params, json_body=json_body if json_body is not None else {})


def unify_http_delete(path: str, params: Optional[Dict[str, Any]] = None) -> str:
    return _request("DELETE", path, params=params, json_body=None)


def _request(
    method: str,
    path: str,
    *,
    params: Optional[Dict[str, Any]] = None,
    json_body: Optional[Dict[str, Any]] = None,
) -> str:
    base = get_unify_api_base_url().rstrip("/")
    if not path.startswith("/"):
        path = "/" + path
    url = base + path
    try:
        with httpx.Client(timeout=20.0, follow_redirects=True) as client:
            req_kw: Dict[str, Any] = {
                "method": method,
                "url": url,
                "params": params,
                "headers": _auth_headers(),
            }
            if json_body is not None:
                req_kw["json"] = json_body
            r = client.request(**req_kw)
    except httpx.RequestError as e:
        return f"校园接口请求失败（{method} {path}）：{e}"

    text = r.text or ""
    if r.status_code == 401:
        return (
            "接口返回未授权(401)。查询新闻、二手或「我想要」等校园数据需要在应用内登录；"
            "请在前端登录后使用小智，或由客户端传入有效 bearer_token。"
        )
    if r.status_code >= 400:
        snippet = text[:500] if text else ""
        return f"校园接口错误 HTTP {r.status_code}（{method} {path}）：{snippet}"

    try:
        data = r.json()
    except Exception:
        return text[:8000] if text else "(空响应)"

    return json.dumps(data, ensure_ascii=False, indent=2)
