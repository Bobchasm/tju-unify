"""与 unify 前端一致的校园网关能力：新闻、二手闲置、交易请求（我想要）。"""

from __future__ import annotations

import json
from typing import Any, Dict, Optional

from langchain_core.tools import tool

from utils.logger_handler import logger
from utils.unify_api_client import unify_http_get, unify_http_post


def _parse_json_obj(s: str) -> Optional[Dict[str, Any]]:
    try:
        v = json.loads(s)
        return v if isinstance(v, dict) else None
    except json.JSONDecodeError:
        return None


@tool(
    description=(
        "查询天津大学校园新闻列表（与首页新闻同源）。"
        "flag 通常为 0；page 从 1 开始。需登录态（网关 JWT）。"
    )
)
def unify_get_school_news(flag: int = 0, page: int = 1) -> str:
    return unify_http_get(
        "/unify-api/news/schoolNews/getByFlag",
        {"flag": int(flag), "page": int(page)},
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
