import os
from abc import ABC
from abc import abstractmethod
from langchain_community.chat_models.tongyi import ChatTongyi, BaseChatModel
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_core.embeddings import Embeddings
from typing import Optional
from utils.config_handler import rag_conf

# 尝试加载环境变量
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    print("Warning: python-dotenv not installed. Using system environment variables only.")


class BaseModelFactory(ABC):
    @abstractmethod
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        pass


def _dashscope_api_key() -> Optional[str]:
    """通义千问 / DashScope 仅支持控制台申请的 API Key（sk- 开头），不是 RAM AccessKey。"""
    key = os.getenv("DASHSCOPE_API_KEY")
    if not key or key.strip() == "" or key == "your_api_key_here":
        return None
    return key


class ChatModelFactory(BaseModelFactory):
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        api_key = _dashscope_api_key()
        if not api_key:
            print("=" * 60)
            print("错误：未配置有效的 DASHSCOPE_API_KEY")
            print("通义千问接口必须使用 DashScope 控制台创建的 API Key，")
            print("不能使用阿里云 RAM 的 AccessKey ID / Secret 代替。")
            print("\n在 .env 中设置：")
            print("  DASHSCOPE_API_KEY=sk-xxxxxxxx")
            print("\n获取地址：https://dashscope.console.aliyun.com/")
            print("=" * 60)
            raise ValueError("Missing DASHSCOPE_API_KEY")

        print("✅ 使用 DashScope API Key 认证")
        return ChatTongyi(
            model=rag_conf["chat_model_name"],
            dashscope_api_key=api_key,
        )


class EmbeddingsFactory(BaseModelFactory):
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        api_key = _dashscope_api_key()
        if not api_key:
            raise ValueError(
                "Missing DASHSCOPE_API_KEY for embeddings (DashScope console API key required)"
            )

        return DashScopeEmbeddings(
            model=rag_conf["embedding_model_name"],
            dashscope_api_key=api_key,
        )


chat_model = ChatModelFactory().generator()
embed_model = EmbeddingsFactory().generator()
