from langchain_core.tools import tool
import random

from rag.vectore_store import VectorStoreService
from rag.rag_service import RagSummarizeService
from utils.logger_handler import logger
from utils.runtime_history import get_history

vector_store = VectorStoreService()
rag = RagSummarizeService(vector_store)

user_ids = ["2021301001", "2021301002", "2021301003", "2021301004", "2021301005",
            "2021301006", "2021301007", "2021301008", "2021301009", "2021301010"]

semester_arr = ["2023-2024-1", "2023-2024-2", "2024-2025-1", "2024-2025-2"]

student_data = {}


@tool(
    description=(
        "从向量库检索天津大学相关公开资料原文（校园生活、校务、综测政策等）。"
        "涉及分值时须结合模块/结构/占比说明，不可把表格数字说成总评直接加这么多。"
    )
)
def rag_summarize(
        query: str,
        strategy: str = "auto",
        use_history: bool = True
) -> str:
    history = None
    if use_history:
        history = get_history()

    return rag.rag_summarize(
        query=query,
        strategy=strategy,
        history=history
    )


@tool(description="高级检索（MQE）：多查询扩展，适合天大校园与校务类问题")
def rag_search_mqe(query: str) -> str:
    return rag.rag_summarize(query, strategy="mqe")


@tool(description="高级检索（HyDE）：假设文档嵌入，适合复杂或表述模糊的问题")
def rag_search_hyde(query: str) -> str:
    return rag.rag_summarize(query, strategy="hyde")


@tool(description="扩展检索：同时启用MQE与HyDE，召回更全")
def rag_search_expanded(query: str) -> str:
    return rag.rag_summarize(query, strategy="expanded")


@tool(description="获取当前用户的学号，以纯字符形式返回")
def get_user_id() -> str:
    return random.choice(user_ids)


@tool(description="获取当前学期，格式为YYYY-YYYY-N，以纯字符形式返回")
def get_current_semester() -> str:
    return "2024-2025-1"


def generate_student_data():
    if not student_data:
        for user_id in user_ids:
            student_data[user_id] = {}
            for semester in semester_arr:
                base_score = random.uniform(85, 95)
                moral_score = random.uniform(88, 98)
                academic_score = random.uniform(80, 95)
                physical_score = random.uniform(85, 95)
                aesthetic_score = random.uniform(85, 95)
                labor_score = random.uniform(88, 95)
                competition_bonus = random.uniform(0, 5)
                research_bonus = random.uniform(0, 3)
                volunteer_bonus = random.uniform(0, 2)
                leadership_bonus = random.uniform(0, 3)
                deduction = random.uniform(0, 1)
                total_score = (base_score * 0.2 + moral_score * 0.2 + academic_score * 0.3 +
                             physical_score * 0.1 + aesthetic_score * 0.1 + labor_score * 0.1 +
                             competition_bonus + research_bonus + volunteer_bonus +
                             leadership_bonus - deduction)
                rank = random.randint(1, 150)
                
                student_data[user_id][semester] = {
                    "基础分": f"{base_score:.2f}",
                    "德育分": f"{moral_score:.2f}",
                    "智育分": f"{academic_score:.2f}",
                    "体育分": f"{physical_score:.2f}",
                    "美育分": f"{aesthetic_score:.2f}",
                    "劳育分": f"{labor_score:.2f}",
                    "竞赛加分": f"{competition_bonus:.2f}",
                    "科研加分": f"{research_bonus:.2f}",
                    "志愿服务加分": f"{volunteer_bonus:.2f}",
                    "学生干部加分": f"{leadership_bonus:.2f}",
                    "扣分": f"{deduction:.2f}",
                    "总分": f"{total_score:.2f}",
                    "年级排名": f"{rank}/450"
                }


@tool(description="检索指定学号、学期的综合素质测评示例记录（演示数据），纯文本返回")
def fetch_student_data(user_id: str, semester: str) -> str:
    generate_student_data()
    try:
        data = student_data[user_id][semester]
        result = f"学号{user_id}在{semester}学期的综合素质测评示例记录：\n"
        result += f"基础分：{data['基础分']}分\n"
        result += f"德育分：{data['德育分']}分\n"
        result += f"智育分：{data['智育分']}分\n"
        result += f"体育分：{data['体育分']}分\n"
        result += f"美育分：{data['美育分']}分\n"
        result += f"劳育分：{data['劳育分']}分\n"
        result += f"竞赛加分：{data['竞赛加分']}分\n"
        result += f"科研加分：{data['科研加分']}分\n"
        result += f"志愿服务加分：{data['志愿服务加分']}分\n"
        result += f"学生干部加分：{data['学生干部加分']}分\n"
        result += f"扣分：{data['扣分']}分\n"
        result += f"综测总分（示例）：{data['总分']}分\n"
        result += f"年级排名：{data['年级排名']}"
        return result
    except KeyError:
        logger.warning(f"[fetch_student_data]未能检索到学生:{user_id}在{semester}的数据。")
        return f"未找到学号{user_id}在{semester}学期的示例记录"


@tool(description="按演示权重计算综合素质测评总分，输入各分项，返回计算过程（示例算法）")
def calculate_score(
    base_score: float = 90.0,
    moral_score: float = 90.0,
    academic_score: float = 85.0,
    physical_score: float = 85.0,
    aesthetic_score: float = 85.0,
    labor_score: float = 90.0,
    competition_bonus: float = 0.0,
    research_bonus: float = 0.0,
    volunteer_bonus: float = 0.0,
    leadership_bonus: float = 0.0,
    deduction: float = 0.0
) -> str:
    total_score = (base_score * 0.2 + moral_score * 0.2 + academic_score * 0.3 + 
                  physical_score * 0.1 + aesthetic_score * 0.1 + labor_score * 0.1 +
                  competition_bonus + research_bonus + volunteer_bonus + 
                  leadership_bonus - deduction)
    
    result = f"综合素质测评分数计算结果（示例公式）：\n"
    result += f"基础分({base_score}) × 20% = {base_score * 0.2:.2f}\n"
    result += f"德育分({moral_score}) × 20% = {moral_score * 0.2:.2f}\n"
    result += f"智育分({academic_score}) × 30% = {academic_score * 0.3:.2f}\n"
    result += f"体育分({physical_score}) × 10% = {physical_score * 0.1:.2f}\n"
    result += f"美育分({aesthetic_score}) × 10% = {aesthetic_score * 0.1:.2f}\n"
    result += f"劳育分({labor_score}) × 10% = {labor_score * 0.1:.2f}\n"
    result += f"竞赛加分：{competition_bonus:.2f}\n"
    result += f"科研加分：{research_bonus:.2f}\n"
    result += f"志愿服务加分：{volunteer_bonus:.2f}\n"
    result += f"学生干部加分：{leadership_bonus:.2f}\n"
    result += f"扣分：-{deduction:.2f}\n"
    result += f"综测总分（示例）：{total_score:.2f}分"
    
    return result


@tool(description="无入参；调用后触发中间件为「学生发展报告」场景注入专用提示词")
def fill_context_for_report():
    return "fill_context_for_report已调用"
