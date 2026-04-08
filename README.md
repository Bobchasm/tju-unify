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
- Fast API

### 二、二手交易市场  
- 商品列表 /sec（按分类、按「最新 / 热门」排序、分页）
- 详情 /getOne
- 浏览量 /click
- 发布 POST sec/issue
- 评论 POST sec/comment

- **未来规划：** 完善接口，加入服务注册Eureka、登录相关拦截与 JWT 配置

### 三、新闻推送
- SchoolNews 的存库与查询
- 对外 /schoolNews/getByFlag（按 flag 分页列表）、/schoolNews/detail（按 id 详情）；
- application.yml 里配置了多条校园新闻栏目 URL；
- **未来规划：** 加入 WebMagic 把爬取结果写入库。

### 四、校园电商平台微服务
- 还没有开始

### 五、团队贡献
- 高灿：二手交易市场本地接口的实现 20%
- 曾意：agent基础框架搭建，高级检索部分与智能体streamlit简易前端测试 20%
- 杨晓越：天大数据文件收集与汇总，PDF处理，向量化入库 20%
- 戴茗静：新闻推送的本地接口实现 20%
- 贾思韵：agent的react中间件 20%

### 六、其他规划
- 前端
- 部署
- 服务治理