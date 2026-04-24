# tju-unify

---

        本项目是一个主要面向tju的校园生活多功能工具应用。前后端分离，后端采用微服务架构，后续可以对工具和应用进行拓展(添加微服务模块/接入网关)。

当前核心功能模块(*标识该功能为网关直接接入)：

- 校园智能体助手

- 二手交易平台

- 新闻推送

- 校园跑腿（起点/终点、小费、期望时间、联系方式；大厅接单）

- 校园电商平台*
  
  该服务也是一个微服务架构的后端
  
  仓库：[https://gitee.com/dai-mingjing/frontend-comprehension](https://gitee.com/dai-mingjing/frontend-comprehension)

---

# 1 项目架构

## 1.1 目录结构

不同框架模块分开，总网关基于Spring实现

```
tju-unify/
├── docs/                  # 一些文档
├── front/                 # 前端源码
├── tian-agent/            # 智能体源码
├── unify-conv/            # Spring部分后端 (包含整个应用网关)
│   ├── conv-api           # Feign 客户端模块
│   ├── conv-common        # 公共模块
│   ├── conv-news          # 新闻推送模块
│   ├── conv-gateway       # 应用网关
│   ├── conv-transaction   # 二手交易模块
│   ├── conv-conv-memo     # 备忘录模块
│   ├── conv-errand        # 校园跑腿模块
│   ├── pom.xml            # 父工程依赖
│   └── sql                # 数据库建库sql文件
├── .gitignore
├── LICENSE
└── README.md
```

# 2 快速启动

## 2.1 前端部分

**环境准备**

- 保证npm可用
  
  可参见 [VUE安装及环境配置（完整版）-CSDN博客](https://blog.csdn.net/qq_52611686/article/details/142653081?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522b9220596f6cbffa1a2f0eb8c533005ed%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=b9220596f6cbffa1a2f0eb8c533005ed&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~top_positive~default-1-142653081-null-null.142%5Ev102%5Epc_search_result_base7&utm_term=vue%E5%AE%89%E8%A3%85%E5%8F%8A%E7%8E%AF%E5%A2%83%E9%85%8D%E7%BD%AE&spm=1018.2226.3001.4187)

- node
  
  可参见 [Node.js安装与配置（详细步骤）_nodejs安装及环境配置-CSDN博客](https://blog.csdn.net/qq_42006801/article/details/124830995?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522363c66d0a867fcac896383171b678743%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=363c66d0a867fcac896383171b678743&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~top_positive~default-1-124830995-null-null.142%5Ev102%5Epc_search_result_base7&utm_term=%E9%85%8D%E7%BD%AEnode&spm=1018.2226.3001.4187)

**快速启动**

```bash
cd 
# 安装依赖
cnpm i
# 启动
npm run dev
```

## 2.2 Spring 后端部分

若出现启动报错，尝试按顺序使用 `maven` 对 `conv-common` `api-common` 进行 `install` 再重启，其他不再详述。

## 2.3 智能体



---

# 3 开发说明

## 3.1 Spring 部分后端

`unify-conv` ：基于Spring系列实现的均在这个模块下管理。主要管理：整个unify的网关(包括外接服务请求处理如电商平台)、新闻推送服务、二手平台服务、后续基于该框架的扩展服务  

### 3.1.1 结构

 `unify-conv` 为这部分的父工程，在这里做依赖版本管理等。  

整体按服务模块组织，feign客户端由各个服务自己管理  

现主要有以下模块：  

- `conv-common` 公共模块，其他服务依赖该模块，有一些公共的依赖已经引入  

- `conv-gateway` 网关，用于路由转发和鉴权  

- `conv-news` 新闻推送服务  

- `conv-transaction`  二手平台服务  

- `conv-memo`   备忘录模块

- `conv-errand`  校园跑腿服务  

- 后续追加服务......  

### 3.1.2 一些说明

关于电商平台，目前直接调用饿了吧那边的接口  

**特别地在用户这一块**  

整个unify的用户管理直接与饿了吧那边同步，也就是unify的用户就是饿了吧的用户，共用一个数据库、一套鉴权逻辑。后续如果有时间的话就把两个分离一下，没有就算了  

**现在网关的鉴权与转发逻辑**  

### 3.1.3 加入新服务流程

**情况一：添加新服务模块实现**

1. 创建新 sprintboot 模块，模块名统一前缀 `conv-`  
   包路径前缀最好规范成 `com.tju.unify.conv.` + 模块名  
   服务名前缀最好是规范成 `unify-`  
   端口：路由7070  
   
   配置文件就直接参考已有服务(nacos、数据库之类的)  

2. 依赖部分：  
      必须引入公共模块，因为需要统一响应等规范；和服务发现 
   
   ```xml
   <dependency>  
       <groupId>com.tju.unify</groupId>       
       <artifactId>conv-common</artifactId>       
       <version>1.0.0-SNAPSHOT</version>   
   </dependency>   
   <dependency>        
       <groupId>com.alibaba.cloud</groupId>        
       <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>   
   </dependency> 
   ```
   
   如果需要获取当前用户信息或者调用其他服务，需要引入feign客户端：
   
   ```xml
   <dependency>  
       <groupId>com.tju.unify</groupId>       
       <artifactId>conv-api</artifactId>       
       <version>1.0.0-SNAPSHOT</version>   
   </dependency>  
   ```
   
   接口文档相关的看着办：
   
   ```xml
   <dependency>  
        <groupId>org.springdoc</groupId>        
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>        
        <version>${springdoc-openapi.version}</version>   
   </dependency>  
   ```

3. 启动类注解，如：  
   
   ```java
   @SpringBootApplication
   @EnableDiscoveryClient // 启用服务发现
   @MapperScan("com.tju.unify.conv.news.mapper") //mapper包路径
   public class ConvNewsApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(ConvNewsApplication.class,args);
       }
   }
   ```

4. 网关接入
   
   在网关的配置文件 `application.yml` 中：
   
   - `spring.cloud.gateway.routes` 添加路由规则
   
   - 对于不需要鉴权的接口，加入 `unify.auth.excludePaths`

5. 数据库暂时用bobchasm.cn服务器上部署的mysql，要新建数据库直接建就行，最好存下建库sql在路径`sql`下  
      然后把mybatis变成mybatis-plus了，基础增删改查可以不用写，然后我看新闻推送那里写了分页，可以改成使用mybatis-plus的更方便的分页  

**情况二：接入外部 api**

直接按情况一的4做

如果想要进行一些处理，可以新建一个模块作为一个包装微服务

### 3.1.4 服务间调用

引入`api` 模块的依赖

- 项目内部服务的接口
  
  在 `/conv-api/src/main/java/com/tju/unify/conv/api/client/inner`中添加微服务的Feign客户端，已经有了就往相应的客户端加接口

- 网关接入的接口
  
  可以在`unify-api` 模块的`src/main/java/com/tju/unify/conv/api/client/outer` 中写调用的逻辑，当然也可以直接在模块里写

## 3.2 智能体

智能体能力由独立服务 **`tian-agent`**（Python / FastAPI）提供，在校园统一登录的前提下，可把「自然语言对话」与网关后的业务接口串起来使用。

### 3.2.1 功能说明

- **多轮对话**：用户可连续追问，助手结合上文作答，适合办事步骤拆解、反复澄清需求等场景。  

- 助手侧可调用网关暴露的 **校园工具接口**（如备忘录等），用户在聊天里用口语描述「记一条备忘」「改提醒时间」等，由智能体解析意图并代为请求后端，**效果上等同于在应用里办事，但入口变成一句话**。  

- **摘要记忆**：较长对话会做摘要持久化，减轻模型上下文压力，同时让跨次进入同一会话时仍能抓住之前讨论过的要点。  

- **完整对话存档**：按 `session_id` 在服务端落盘完整消息列表（有条数上限），换浏览器或清理本地缓存后，仍可通过会话列表或「刷新当前会话」**恢复历史气泡内容**。  

- **与二手市场联动**：用户可以询问二手市场的交易，小智助手可以直接调用后端接口，并用自然语言回复

- **与新闻信息联动**：用户可以询问天大今日新闻，小智助手将调用新闻相关接口，并将今日新闻展示给用户

### 3.2.2 相关技术

**`tian-agent`** 的技术栈：检索、ReAct、记忆，以及 **HTTP 接入、模型与配置、校园工具调用、提示词与 RAG 编排、日志观测** 等，整体关系：**网关鉴权上下文 → FastAPI 会话与流式 → Agent 循环 → 工具（校园 API / RAG / 演示工具）→ 记忆**。

#### 检索（RAG）

- **基础检索**：**Chroma** + 向量相似度；支持将 **txt / pdf / csv** 等资料入库并向量检索，入口服务见 `rag/vectore_store.py`（文件名如此）。  
- **高级检索**（`rag/advanced_retrieval.py`）：  
  - **MQE（多查询扩展）**：由模型生成多条语义相近的子问句，多路检索后提高召回。  
  - **HyDE（假设文档嵌入）**：先根据问题生成假设性「答案片段」，再对其做嵌入并检索相似文档，适合表述模糊或需「猜意图」的查询。  
  - **扩展检索**：在配置允许时组合 **MQE + HyDE** 等多路结果，**合并、去重并排序**，得到更稳的候选文档集。  
- 工具层在 `agent/tools/agent_tools.py` 中暴露了 `rag_summarize`、`rag_search_mqe`、`rag_search_hyde`、`rag_search_expanded` 等封装，供智能体按需调用。

#### ReAct Agent

- **作用**：实现「**推理 + 行动**」循环——模型根据当前对话决定**直接回答**或**调用工具**（校园接口、RAG 等），并支持**多轮工具调用**直至给出最终答复。  
- **实现**：基于 **LangChain** 的 `create_agent`（运行栈与 **LangGraph** 生态衔接；中间件等类型来自 `langgraph` / `langchain.agents`），组合 **聊天模型 + 系统提示词 + 工具列表 + 中间件**。核心类为 `agent/react_agent.py` 中的 **`ReactAgent`**，对上通过 **`execute_stream`** 等形式提供**流式**输出（与 `main.py` 中组装的 `agent_messages` 衔接）。  
- **中间件**（`agent/tools/middleware.py`）：  
  - **`monitor_tool`**：工具调用前后打日志；在调用 `fill_context_for_report` 成功后会将运行时上下文中的 `report` 置为 `True`，便于报告类场景切换提示词。  
  - **`log_before_model`**：每次调用模型前记录消息条数及最后一条用户/助手内容摘要；同时将当前 `state["messages"]` 中的用户与助手文本写入运行时历史（见下节）。  
  - **`report_prompt_switch`**：根据上下文是否处于报告模式，**动态切换**系统提示（普通 `system` vs `report`）。

#### 记忆功能

- **滑动窗口**：配置为保留最近 **10 轮**完整对话轮次作为窗口（与 `utils/conversation_memory.py`、`config/agent.yml` 中 `conversation_window_rounds` 一致），超出部分进入摘要逻辑。  
- **更早历史**：由对话摘要提示词将较早内容**压缩为摘要记忆**，与最近窗口一起拼进模型上下文，形成「**摘要 + 最近 10 轮 + 当前问题**」的输入结构。  
- **摘要持久化**：每个会话使用 API 下发的 **`session_id`**；启用持久化时，摘要读写默认路径为 **`data/conversation_memory/<session_id>.json`**（`utils/conversation_summary_store.py` + `main.py`），实现**刷新页面甚至重启服务**后仍能恢复该会话的摘要记忆。  
- **完整对话存档（与摘要分离）**：流式/非流式每轮结束后可将 user/assistant 全文落盘至 **`data/chat_history/`**（见 `utils/chat_history_store.py` 与 `main.py` 中历史接口），用于前端「历史对话」列表与恢复气泡内容。  
- **运行时历史与 RAG 摘要集成**（`utils/runtime_history.py`）：通过 **`contextvars`** 保存当前请求/图执行链路内的对话片段；中间件在 **`log_before_model`** 阶段把本轮可序列化的 user/assistant 消息写入运行时历史，`rag_summarize` 工具在 `use_history=True` 时 **`get_history()`** 读出并传入 `rag.rag_summarize(history=...)`，使检索改写能基于**完整对话上下文**，而不是仅对当前单句 query 做扩展。

#### 服务入口与 HTTP API（`main.py`）

- **FastAPI** 应用：统一 CORS，挂载健康检查等基础路由。  
- **对话**：`POST /api/chat`（非流式）、`POST /api/chat/stream`（**SSE 流式**）；流式首帧可下发 **`session_id`**，供前端多轮与历史恢复。  
- **历史与会话列表**：`GET /api/chat/history`、`GET /api/chat/sessions`、`DELETE /api/chat/history`，与 `chat_history_store`、前端「历史」抽屉配合。  
- **鉴权透传**：请求体中的 **`bearer_token`** 经 **`utils/unify_api_context.py`** 注入当前协程上下文，工具内 HTTP 访问 **`/unify-api/**`** 时携带与前端一致的 JWT。

#### 模型与全局配置

- **`model/factory.py`**：构造对话模型、嵌入模型等，供 Agent、RAG、高级检索共用。  
- **`utils/config_handler.py`**：加载 **`config/agent.yml`**、**`config/retrieval.yml`** 及 prompts 相关 YAML，驱动窗口轮数、摘要开关与目录、检索策略、外部网关基地址（如 **`UNIFY_API_BASE_URL`**）等。

#### 校园业务工具与统一接口（`agent/tools/unify_campus_tools.py`）

- 将 **新闻、二手、备忘** 等能力封装为 LangChain **Tool**，由 `ReactAgent` 注册；内部通过 **`utils/unify_api_client.py`** 访问校园网关，与前端 **`/unify-api`** 前缀对齐。  
- 与「演示/综测」类工具（`agent_tools.py` 中的学号、学期、报告上下文等）并存，便于扩展更多 **`unify_*`** 工具而不改 Agent 骨架。

#### 提示词与 RAG 编排

- **`utils/prompt_loader.py`**：加载 **`prompts/`** 下系统提示、对话摘要提示、报告类提示等文本，供 `ReactAgent` 与摘要链路使用。  
- **`rag/rag_service.py`**（如 **`RagSummarizeService`**）：按策略调用向量库与 **`advanced_retrieval`**，组织检索结果并交给模型生成引用式回答，与工具层的 `rag_summarize` / 分策略工具衔接。

#### 日志与观测

- **`utils/logger_handler.py`**：统一日志；中间件 **`monitor_tool`**、**`log_before_model`** 会输出工具名、参数与调用模型前的消息概况，便于本地或服务器上排查「调了哪个工具、上下文多长」。

---

# 4 一些说明

欢迎贡献代码、报告问题或提出建议！

### 4.1 提交问题

- 描述问题现象
- 提供复现步骤
- 附上错误日志
- 说明环境信息

### 4.2 提交代码

1. Fork本仓库
2. 创建功能分支
3. 提交代码并编写测试
4. 提交Pull Request

### 4.3 未来畅想

欢迎fork本仓库，并提交新的工具业务，乐意接收！   
例如 ：

- 课程评价
- 跑腿业务（目前已完成）感谢 @ZacheryPole666 
- 宿舍保修
- 失物招领
- 成绩查询

### 4.4 联系方式

如有问题或建议，请通过以下方式联系：

- **邮箱**：  
  zengyicydd@tju.edu.cn  <br>
  gaocan@tju.edu.cn  <br>
  daimingjing142857@tju.edu.cn  <br>
  yxy641121@gmail.com  <br>
  jsyy@tju.edu.cn

- **Gitee Issues**：提交问题到项目仓库
