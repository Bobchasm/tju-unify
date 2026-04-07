from typing import Callable, Any
from langchain.agents import AgentState
from langchain.agents.middleware import wrap_tool_call, before_model, dynamic_prompt, ModelRequest
from langchain.tools.tool_node import ToolCallRequest
from langchain_core.messages import ToolMessage
from langgraph.runtime import Runtime
from langgraph.types import Command
# from utils.logger_handler import logger
from utils.logger_handler import logger
from utils.prompt_loader import load_system_prompt, load_report_prompt


@wrap_tool_call
def monitor_tool(
        request: ToolCallRequest,
        handler: Callable[[ToolCallRequest], ToolMessage | Command]
) -> ToolMessage | Command:
    logger.info(f"[tool monitor]执行工具: {request.tool_call['name']}")
    logger.info(f"[tool monitor]参数: {request.tool_call['args']}")
    try:
        result = handler(request)
        logger.info(f"[tool monitor]工具{request.tool_call['name']}调用成功")

        if request.tool_call['name'] == 'fill_context_for_report':
            logger.info(f"[tool monitor]fill_context_for_report工具被调用，注入上下文 report=True")
            request.runtime.context["report"] = True
        return result
    except Exception as e:
        logger.info(f"工具{request.tool_call['name']}调用失败: {e}")
        raise


@before_model
def log_before_model(state:AgentState, runtime: Runtime) -> dict[str, Any] | None:
    logger.info(f"[log_before_model]: 即将调用模型，带有{len(state['messages'])}条消息，消息如下：")
    # for message in state['messages']:
    #     logger.info(f"[log_before_model][{type(message).__name__}]: {message.content.strip()}")
    logger.info(f"[log_before_model]: ----------省略已输出内容----------")
    logger.info(f"[log_before_model][{type(state['messages'][-1]).__name__}]: {state['messages'][-1].content.strip()}")


    return None

@dynamic_prompt
def report_prompt_switch(request: ModelRequest) -> str:
    is_report = request.runtime.context.get("report", False)
    if is_report:
        return load_report_prompt()

    return load_system_prompt()


