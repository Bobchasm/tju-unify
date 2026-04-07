import os

def get_project_root() -> str:
    """
    获取工程根目录（无论脚本在哪个目录运行，都能返回正确的根目录）
    原理：基于当前文件的绝对路径，向上推导到工程根目录
    """
    # 当前文件（path_utils.py）的绝对路径
    current_file = os.path.abspath(__file__)
    # 当前文件所在目录（utils/）
    current_dir = os.path.dirname(current_file)
    # 工程根目录（utils/ 的上一级）
    project_root = os.path.dirname(current_dir)
    return project_root

def get_abs_path(relative_path: str) -> str:
    """
    将工程内的相对路径转为绝对路径（统一路径基准）
    :param relative_path: 相对于工程根目录的路径，如 "config/rag.yml"
    :return: 绝对路径
    """
    project_root = get_project_root()
    return os.path.join(project_root, relative_path)
