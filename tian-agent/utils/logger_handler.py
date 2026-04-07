# logger_utils.py
import logging
import os
import re
from datetime import datetime
from typing import Optional
from utils.path_tools import get_abs_path

# 日志保存根目录
LOG_ROOT = get_abs_path("logs")
# 确保日志目录存在
os.makedirs(LOG_ROOT, exist_ok=True)

# 日志格式配置（包含时间、模块、行号，便于调试Agent）
DEFAULT_LOG_FORMAT = logging.Formatter(
    "%(asctime)s - %(name)s - %(levelname)s - %(filename)s:%(lineno)d - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)


def mask_sensitive_data(text: str) -> str:
    """
    日志脱敏函数：隐藏API Key、手机号、邮箱等敏感信息
    :param text: 原始文本
    :return: 脱敏后的文本
    """
    if not isinstance(text, str):
        return text

    # 脱敏OpenAI/通义千问API Key（sk-开头）
    text = re.sub(r"sk-\w+", "sk-******", text)
    # 脱敏手机号
    text = re.sub(r"1[3-9]\d{9}", "1**********", text)
    # 脱敏邮箱
    text = re.sub(r"(\w+)@(\w+)\.(\w+)", r"\1****@\2.\3", text)
    # 脱敏密码/密钥（password/key=开头）
    text = re.sub(r"(password|key|secret)=[^& ]+", r"\1=******", text)
    return text


class SensitiveDataFilter(logging.Filter):
    """日志过滤器：自动脱敏日志中的敏感信息"""

    def filter(self, record: logging.LogRecord) -> bool:
        # 对日志消息脱敏
        if record.msg:
            record.msg = mask_sensitive_data(record.msg)
        # 对日志参数脱敏（如果有）
        if record.args:
            record.args = tuple(mask_sensitive_data(arg) for arg in record.args)
        return True


def get_logger(
        name: str = "agent",
        console_level: int = logging.INFO,
        file_level: int = logging.DEBUG,
        log_file: Optional[str] = None
) -> logging.Logger:
    """
    获取配置好的日志器（开箱即用）
    :param name: 日志器名称（建议按模块命名，如agent.tools/agent.rag/agent.llm）
    :param console_level: 控制台日志级别（默认INFO，开发时可设为DEBUG）
    :param file_level: 文件日志级别（默认DEBUG，记录详细信息）
    :param log_file: 自定义日志文件名（默认按日期生成：agent_20240121.log）
    :return: 配置完成的Logger对象
    """
    # 1. 创建/获取日志器
    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)  # 全局最低级别
    logger.addFilter(SensitiveDataFilter())  # 添加脱敏过滤器

    # 避免重复添加Handler（多次导入时只配置一次）
    if logger.handlers:
        return logger

    # 2. 配置控制台Handler（开发调试用）
    console_handler = logging.StreamHandler()
    console_handler.setLevel(console_level)
    console_handler.setFormatter(DEFAULT_LOG_FORMAT)
    logger.addHandler(console_handler)

    # 3. 配置文件Handler（生产环境留存日志）
    if not log_file:
        log_file = os.path.join(LOG_ROOT, f"{name}_{datetime.now().strftime('%Y%m%d')}.log")

    file_handler = logging.FileHandler(log_file, encoding="utf-8")
    file_handler.setLevel(file_level)
    file_handler.setFormatter(DEFAULT_LOG_FORMAT)
    logger.addHandler(file_handler)

    return logger


# 快捷获取默认Agent日志器
logger = get_logger("agent")