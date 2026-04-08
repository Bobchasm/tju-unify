from utils.config_handler import prompts_conf
from utils.path_tools import get_abs_path
from utils.logger_handler import logger


def load_system_prompt():
    try:
        system_prompt_path = get_abs_path(prompts_conf["main_prompt_path"])
    except KeyError as e:
        logger.error(f"[load_system_prompt]解析系统提示词文件路径失败。")
        raise e

    try:
        return open(system_prompt_path, "r", encoding="utf-8").read()
    except FileNotFoundError as e:
        logger.error(f"[load_system_prompt]系统提示词文件{system_prompt_path}不存在. {str(e)}")
        raise e
    except Exception as e:
        logger.error(f"[load_system_prompt]解析系统提示词{system_prompt_path}失败. {str(e)}")
        raise e


def load_report_prompt():
    try:
        report_prompt_path = get_abs_path(prompts_conf["report_prompt_path"])
    except KeyError as e:
        logger.error(f"[report_prompt_path]解析系统提示词文件路径失败。")
        raise e

    try:
        return open(report_prompt_path, "r", encoding="utf-8").read()
    except FileNotFoundError as e:
        logger.error(f"[report_prompt_path]报告提示词文件{report_prompt_path}不存在. {str(e)}")
        raise e
    except Exception as e:
        logger.error(f"[report_prompt_path]解析报告提示词{report_prompt_path}失败. {str(e)}")
        raise e
