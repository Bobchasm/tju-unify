import yaml
from utils.path_tools import get_abs_path


class ConfigHandler(object):
    @staticmethod
    def load_rag_config(config_path: str = get_abs_path("config/rag.yml"), encoding="utf-8"):
        with open(config_path, "r", encoding=encoding) as f:
            return yaml.load(f.read(), Loader=yaml.FullLoader)

    @staticmethod
    def load_chroma_config(config_path: str = get_abs_path("config/chroma.yml"), encoding="utf-8"):
        with open(config_path, "r", encoding=encoding) as f:
            return yaml.load(f.read(), Loader=yaml.FullLoader)

    @staticmethod
    def load_prompts_config(config_path: str = get_abs_path("config/prompts.yml"), encoding="utf-8"):
        with open(config_path, "r", encoding=encoding) as f:
            return yaml.load(f.read(), Loader=yaml.FullLoader)

    @staticmethod
    def load_agent_config(config_path: str = get_abs_path("config/agent.yml"), encoding="utf-8"):
        with open(config_path, "r", encoding=encoding) as f:
            return yaml.load(f.read(), Loader=yaml.FullLoader)

    @staticmethod
    def load_retrieval_config(config_path: str = get_abs_path("config/retrieval.yml"), encoding="utf-8"):
        """加载高级检索策略配置"""
        with open(config_path, "r", encoding=encoding) as f:
            return yaml.load(f.read(), Loader=yaml.FullLoader)


    @staticmethod
    def load_all_configs():
        """一次性加载所有配置"""
        return {
            "rag": ConfigHandler.load_rag_config(),
            "chroma": ConfigHandler.load_chroma_config(),
            "prompts": ConfigHandler.load_prompts_config(),
            "agent": ConfigHandler.load_agent_config(),
            "retrieval": ConfigHandler.load_retrieval_config()
        }


# 加载所有配置
rag_conf = ConfigHandler.load_rag_config()
chroma_conf = ConfigHandler.load_chroma_config()
prompts_conf = ConfigHandler.load_prompts_config()
agent_conf = ConfigHandler.load_agent_config()
retrieval_conf = ConfigHandler.load_retrieval_config()