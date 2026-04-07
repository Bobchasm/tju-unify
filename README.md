# tju-unify
## 2026.4.7进度

### 一 、智能体
1.完成基本框架搭建  

2.检索部分
  - 基础检索：Chroma + 向量相似度，支持 txt/pdf/csv 入库（rag/vectore_store.py）
  - 高级检索（rag/advanced_retrieval.py）：
    - MQE：多查询扩展，生成多条语义等价问句提高召回。
    - HyDE：假设文档嵌入，先生成假设答案再检索相似文档。
    - 扩展检索框架：MQE + HyDE 多路检索后合并去重、排序。
    

3.ReAct Agent  
- **作用：** 实现「推理 + 行动」循环：根据当前对话决定下一步是调用工具还是直接回答，支持多轮工具调用。
- **实现：** 基于 LangChain create_agent（LangGraph），模型 + 系统提示词 + 工具列表 + 中间件。
- **中间件（agent/tools/middleware.py）**：
  - monitor_tool：工具调用前后打日志，并可改写上下文（如 report 场景）。
  - log_before_model：每次调用模型前记录消息条数及简要内容。
  - report_prompt_switch：按上下文动态切换/注入报告相关 Prompt。
- **入口：** agent/react_agent.py 的 ReactAgent，对外提供 execute_stream(query) 流式输出。
  ![img.png](img.png)

4.未来规划

- 工具调用：调用后端其他接口，例如，当用户意图涉及“跑腿下单”“空教室查询”等操作类需求时，系统可自动调用对应后端API完成服务闭环，实现从信息咨询到业务办理的功能延伸。
- 增加记忆功能：使系统具备对话记忆与上下文理解能力