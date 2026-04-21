"""HTTP 调用 unify-gateway 时使用的 JWT 上下文（单次对话请求内有效）。"""

from __future__ import annotations

from contextlib import contextmanager
from contextvars import ContextVar
from typing import Iterator, Optional

_bearer_token_var: ContextVar[Optional[str]] = ContextVar("unify_bearer_token", default=None)


def get_unify_bearer_token() -> Optional[str]:
    return _bearer_token_var.get()


@contextmanager
def unify_api_context(bearer_token: Optional[str] = None) -> Iterator[None]:
    token = _bearer_token_var.set(bearer_token)
    try:
        yield
    finally:
        _bearer_token_var.reset(token)
