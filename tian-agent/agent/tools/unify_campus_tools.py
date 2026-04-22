"""与 unify 前端一致的校园网关能力：新闻、二手闲置、交易请求（我想要）。"""

from __future__ import annotations

import json
from typing import Any, Dict, Optional

from langchain_core.tools import tool

from utils.logger_handler import logger
from utils.unify_api_client import unify_http_get, unify_http_post, unify_http_put


def _parse_json_obj(s: str) -> Optional[Dict[str, Any]]:
    try:
        v = json.loads(s)
        return v if isinstance(v, dict) else None
    except json.JSONDecodeError:
        return None


@tool(
    description=(
        "查询天津大学校园新闻分页列表（与首页新闻同源）。"
        "返回 JSON 的 data 含：records（新闻数组）、total、current、size、pages。"
        "flag 通常为 0 表示全部；page 从 1 开始；size 为每页条数，默认 8、最大 50。需登录态（网关 JWT）。"
    )
)
def unify_get_school_news(flag: int = 0, page: int = 1, size: int = 8) -> str:
    sz = int(size)
    if sz < 1:
        sz = 8
    if sz > 50:
        sz = 50
    return unify_http_get(
        "/unify-api/news/schoolNews/getByFlag",
        {"flag": int(flag), "page": int(page), "size": sz},
    )


@tool(
    description=(
        "按 id 查询校园新闻详情。id 为字符串（与后端一致）。需登录态。"
    )
)
def unify_get_school_news_detail(news_id: str) -> str:
    return unify_http_get(
        "/unify-api/news/schoolNews/detail",
        {"id": news_id.strip()},
    )


@tool(
    description=(
        "在二手/闲置广场按关键词搜索帖子（标题等）。需登录态。"
    )
)
def unify_search_secondhand_posts(keyword: str) -> str:
    kw = (keyword or "").strip()
    if not kw:
        return "搜索关键词不能为空。"
    return unify_http_get(
        "/unify-api/transaction/post/search",
        {"keyword": kw},
    )


@tool(
    description=(
        "分页浏览二手/闲置帖子列表。page_no 从 1 开始，每页条数由后端决定。需登录态。"
    )
)
def unify_list_secondhand_posts(page_no: int = 1) -> str:
    return unify_http_get(
        "/unify-api/transaction/post/list",
        {"pageNo": int(page_no)},
    )


@tool(
    description="按帖子 id 获取二手帖详情（含卖家 userId、标题、价格等）。需登录态。"
)
def unify_get_secondhand_post_detail(post_id: int) -> str:
    return unify_http_get(
        "/unify-api/transaction/post/detail",
        {"id": int(post_id)},
    )


@tool(
    description=(
        "对指定帖子发起「我想要」交易请求（与商品详情页按钮一致）。"
        "仅需 post_id；会自动拉取帖子的卖家 id。"
        "必须已登录；不能对自己的帖子发起请求时由后端规则约束。"
    )
)
def unify_submit_trade_request(post_id: int) -> str:
    detail_raw = unify_http_get(
        "/unify-api/transaction/post/detail",
        {"id": int(post_id)},
    )
    detail = _parse_json_obj(detail_raw)
    if not detail:
        return detail_raw

    if not detail.get("success"):
        return detail_raw

    post = detail.get("data")
    if not isinstance(post, dict):
        return f"帖子详情格式异常：{detail_raw[:1200]}"

    seller_id = post.get("userId")
    if seller_id is None:
        return "帖子详情中缺少卖家 userId，无法提交「我想要」。"

    body = {
        "postId": int(post_id),
        "sellerId": int(seller_id) if isinstance(seller_id, (int, float)) else seller_id,
        "status": 0,
    }
    logger.info("[unify_submit_trade_request] POST /request/add body=%s", body)
    return unify_http_post("/unify-api/transaction/request/add", body)


@tool(
    description="列出某帖子下所有「我想要」交易请求（买家/卖家沟通前可查进度）。需登录态。"
)
def unify_list_trade_requests_for_post(post_id: int) -> str:
    return unify_http_get(
        "/unify-api/transaction/request/list",
        {"postId": int(post_id)},
    )


@tool(
    description=(
        "读取当前用户备忘录聚合快照：近一周勾选子任务数、近一周有更新的备忘数、待处理提醒、置顶与最近备忘标题。"
        "用于关心用户进度、避免重复追问；需登录。"
    )
)
def unify_memo_get_agent_snapshot() -> str:
    return unify_http_get("/unify-api/memo/agent/snapshot", None)


@tool(
    description=(
        "为用户创建一条备忘录，可带提醒时间与多条子任务（与 App 备忘录同源）。需登录。"
        "title 必填；content 可为空；remind_at_iso 为提醒时间，格式如 2026-04-25T15:00:00，无提醒则传空字符串；"
        "subtasks_json 为 JSON 数组字符串，例如 [\"买牛奶\",\"取快递\"] 或 []。"
        "当用户用自然语言说「提醒我周五下午三点去取快递顺便买牛奶」时，你应先解析出时间与事项，再调用本工具。"
    )
)
def unify_memo_create_with_reminder(
    title: str,
    content: str = "",
    remind_at_iso: str = "",
    subtasks_json: str = "[]",
) -> str:
    t = (title or "").strip()
    if not t:
        return "创建失败：标题不能为空。"
    raw = (subtasks_json or "[]").strip()
    try:
        arr = json.loads(raw)
    except json.JSONDecodeError:
        return f"子任务 JSON 无法解析：{raw[:200]}"
    if not isinstance(arr, list):
        return "subtasks_json 必须是 JSON 数组，例如 [\"买牛奶\"]。"
    tasks = []
    for i, item in enumerate(arr):
        if isinstance(item, str) and item.strip():
            tasks.append({"title": item.strip(), "done": False, "sortOrder": i})
    body: Dict[str, Any] = {
        "title": t,
        "content": (content or "").strip(),
        "pinned": False,
        "sortOrder": 0,
        "tasks": tasks,
    }
    ra = (remind_at_iso or "").strip()
    if ra:
        body["remindAt"] = ra if len(ra) > 16 else f"{ra}:00" if len(ra) == 16 else ra
    logger.info("[unify_memo_create_with_reminder] POST /memo/add body keys=%s", list(body.keys()))
    return unify_http_post("/unify-api/memo/add", body)


@tool(
    description=(
        "将某条备忘的某条子任务标记为已完成或未完成。需登录。"
        "task_id 为子任务数字 id；done 为 true/false。"
    )
)
def unify_memo_set_task_done(task_id: int, done: bool) -> str:
    return unify_http_put(
        f"/unify-api/memo/task/{int(task_id)}/done",
        {},
        {"done": done},
    )
