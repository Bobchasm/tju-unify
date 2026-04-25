# TJU-Unify 全项目部署指南

> 涵盖 `front/`（Vue 前端）、`unify-conv/`（Spring Cloud 后端）、`tian-agent/`（AI 智能体）三个子系统的完整部署。

---

## 一、环境总览

### 必需基础设施

| 组件 | 最低版本 | 用途 | 说明 |
|------|---------|------|------|
| **JDK** | 17 | Spring Boot 编译 & 运行 | 必须 17，项目锁定 `java.version=17` |
| **Maven** | 3.6+ | Java 构建 | 父 POM 配置阿里云镜像，国内无需额外 settings |
| **Node.js** | 16+ | 前端构建 & 开发 | npm 可用 |
| **Python** | 3.10 | 智能体服务 | `pyrightconfig.json` 锁定 3.10 |
| **MySQL** | 8.x | 业务数据存储 | 驱动 `mysql-connector-j 8.0.33`，4 个独立数据库 |
| **Nacos** | 2.x | 服务注册 & 发现 | 所有业务服务 + Gateway 都依赖 Nacos |

### 外部服务依赖

| 服务 | 地址（当前配置） | 作用 |
|------|-----------------|------|
| DashScope API | 阿里云 DashScope 平台 | 智能体 LLM（qwen3-max）+ Embedding（text-embedding-v2） |
| 电商平台网关 | `http://bobchasm.cn:8080/api/user/current` | 用户身份解析（JWT → 用户 ID），由 `ElmUserClient` 调用 |

### 端口规划

| 服务 | 端口 | 对外暴露 |
|------|------|---------|
| Nacos Server | 8848 | 仅内网 |
| MySQL | 3306 | 仅内网 |
| conv-gateway | **7070** | ✅ 前端 & Agent 的统一入口 |
| conv-transaction | 7771 | 仅通过 Gateway |
| conv-news | 7772 | 仅通过 Gateway |
| conv-memo | 7773 | 仅通过 Gateway |
| conv-errand | 7774 | 仅通过 Gateway |
| tian-agent | **8000** | ✅ 前端直连（未经过 Gateway） |
| 前端 dev server | 5173 | 仅开发环境 |

---

## 二、基础设施准备

### 2.1 MySQL 建库

连接到 MySQL 服务器后，依次创建 4 个数据库并导入建表 SQL：

```bash
# 在 MySQL 客户端中执行
CREATE DATABASE IF NOT EXISTS unify_transaction CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS unify_news          CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS unify_memo          CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS unify_errand         CHARACTER SET utf8mb4;
```

然后按库名对应导入：

```bash
mysql -u<user> -p<pass> unify_transaction < unify-conv/sql/unify_transaction.sql
mysql -u<user> -p<pass> unify_news          < unify-conv/sql/unify_news.sql
mysql -u<user> -p<pass> unify_memo          < unify-conv/sql/unify_memo.sql
mysql -u<user> -p<pass> unify_errand         < unify-conv/sql/unify_errand.sql
```

可选：备忘录演示数据

```bash
mysql -u<user> -p<pass> unify_memo < unify-conv/sql/unify_memo_seed_demo.sql
```

增量变更（如已有 transaction 表需追加 images 列）：

```bash
mysql -u<user> -p<pass> unify_transaction < unify-conv/sql/alter_add_images_column.sql
```

> ⚠️ **用户表不在上述库中**。用户数据由外部电商平台 MySQL 管理，`unify-conv` 只读取 JWT 中的用户名，通过 HTTP 调用电商网关获取用户 ID。

### 2.2 Nacos 部署

1. 下载 Nacos 2.x（https://nacos.io）
2. 以单机模式启动：

```bash
sh startup.sh -m standalone
```

3. 如需持久化 Nacos 配置到 MySQL，导入 `unify-conv/sql/nacos_config.sql` 到 Nacos 专用数据库，并修改 Nacos 的 `application.properties` 中 `spring.datasource` 配置。
4. 确保 Nacos 可访问于 `bobchasm.cn:8848`（或修改所有服务的 `application.yml` 中的 `server-addr`）。

> 当前所有服务硬编码 Nacos 地址 `bobchasm.cn:8848`、用户名 `nacos`、密码 `nacos`。若自建 Nacos，需逐个修改 5 个服务的配置。

### 2.3 DashScope API Key

智能体依赖阿里云 DashScope：

1. 登录 https://dashscope.console.aliyun.com 获取 API Key（`sk-` 前缀的 Console Key，不是 RAM AccessKey）
2. 配置方式任选其一：
   - **环境变量**：`export DASHSCOPE_API_KEY=sk-your-key-here`
   - **`.env` 文件**：在 `tian-agent/` 目录下创建 `.env`，写入 `DASHSCOPE_API_KEY=sk-your-key-here`
   - `model/factory.py` 会在模块加载时通过 `dotenv` 或系统环境变量读取此值；缺失则启动报错

---

## 三、各子系统部署步骤

### 3.1 Spring Cloud 后端（`unify-conv/`）

#### 编译打包

```bash
cd unify-conv

# ① 先安装公共依赖（顺序不能跳过，否则后续模块编译报 class-not-found）
cd conv-common && mvn install && cd ..
cd conv-api    && mvn install && cd ..

# ② 全量打包
mvn install -DskipTests=true
```

产物位置：各模块 `target/<artifactId>-1.0.0-SNAPSHOT.jar`

#### 配置修改（生产部署时）

**方案 A：命令行参数覆盖**

```bash
java -jar conv-transaction-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://YOUR_HOST:3306/unify_transaction \
  --spring.datasource.username=YOUR_USER \
  --spring.datasource.password=YOUR_PASS \
  --spring.cloud.nacos.discovery.server-addr=YOUR_NACOS:8848
```

**方案 B：环境变量覆盖**（仅 conv-memo / conv-errand 支持）

```bash
export MEMO_DB_HOST=your-mysql-host
export MEMO_DB_USER=your-user
export MEMO_DB_PASSWORD=your-pass

export ERRAND_DB_HOST=your-mysql-host
export ERRAND_DB_USER=your-user
export ERRAND_DB_PASSWORD=your-pass
```

> ⚠️ **conv-transaction 和 conv-news 的数据库配置没有环境变量覆盖机制**，只能通过命令行参数或修改 `application.yml`。

#### 启动顺序

```bash
# Nacos 必须先就绪，否则服务注册失败
# Gateway 可最先或最后启动；业务服务启动后会自动注册到 Nacos

# 推荐顺序：
java -jar conv-news/target/conv-news-1.0.0-SNAPSHOT.jar &
java -jar conv-transaction/target/conv-transaction-1.0.0-SNAPSHOT.jar &
java -jar conv-memo/target/conv-memo-1.0.0-SNAPSHOT.jar &
java -jar conv-errand/target/conv-errand-1.0.0-SNAPSHOT.jar &
java -jar conv-gateway/target/conv-gateway-1.0.0-SNAPSHOT.jar &
```

> Gateway 无数据源依赖（启动类排除了 `DataSourceAutoConfiguration`），不需要 MySQL 连接。

#### Gateway 路由 & 鉴权

Gateway (`application.yml`) 已配置路由规则：

- `/unify-api/news/**` → `unify-news`
- `/unify-api/transaction/**` → `unify-transaction`
- `/unify-api/memo/**` → `unify-memo`
- `/unify-api/errand/**` → `unify-errand`
- `/api/**` → 外部电商网关

免鉴权路径（`unify.auth.excludePaths`）：

- `/api/**`（电商接口由电商侧鉴权）
- `/ws/**`
- `/unify-api/news/schoolNews/**`

其他 `/unify-api/**` 路径需要 JWT。

### 3.2 AI 智能体（`tian-agent/`）

#### 安装依赖

```bash
cd tian-agent
pip install -r requirements.txt
```

> `requirements.txt` 中的 `chromadb`、`streamlit` 等依赖可能需要较长时间安装，建议在干净虚拟环境中操作：
> ```bash
> python -m venv .venv
> source .venv/bin/activate
> pip install -r requirements.txt
> ```

#### 配置

| 配置文件 | 路径 | 关键项 |
|---------|------|--------|
| `agent.yml` | `config/agent.yml` | `unify_api_base_url`（Gateway 地址，默认 `http://127.0.0.1:7070`）、`conversation_window_rounds`、持久化目录 |
| `rag.yml` | `config/rag.yml` | `chat_model_name`（`qwen3-max`）、`embedding_model_name`（`text-embedding-v2`） |
| `chroma.yml` | `config/chroma.yml` | `persist_directory`（向量库路径 `chroma_db/`）、`chunk_size`/`chunk_overlap` |
| `retrieval.yml` | `config/retrieval.yml` | MQE/HyDE 开关、缓存配置 |
| `prompts.yml` | `config/prompts.yml` | 各提示词文件路径 |

#### 环境变量

| 变量 | 必须 | 默认值 | 说明 |
|------|------|--------|------|
| `DASHSCOPE_API_KEY` | ✅ | — | DashScope Console Key（`sk-` 前缀） |
| `UNIFY_API_BASE_URL` | ❌ | `http://127.0.0.1:7070` | 覆盖 `agent.yml` 中的 Gateway 地址 |

#### 知识库初始化（可选）

如需 RAG 检索能力，先将文档入库：

```bash
# 修改 config/chroma.yml 中的 data_path 指向文档目录
# 文档放入 data/ 目录（支持 txt/pdf/csv/json）
python convert_pdfs.py  # 或按 chroma.yml 配置由服务自动加载
```

> 修改 `chunk_size` 或 `chunk_overlap` 后，**必须删除 `chroma_db/` 和 `md5.text` 再重新加载**。

#### 启动

```bash
cd tian-agent
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

启动后访问 `http://localhost:8000/docs` 查看 API 文档。

> ⚠️ **模型在模块加载时初始化**（`factory.py` 在 import 阶段就调用 DashScope），`DASHSCOPE_API_KEY` 必须在启动前就可用。

#### 运行时数据（不要提交到 Git）

| 路径 | 内容 |
|------|------|
| `data/chat_history/` | 按 session_id 存的完整对话存档 |
| `data/conversation_memory/` | 按 session_id 存的摘要记忆 |
| `chroma_db/` | Chroma 向量库持久化 |
| `logs/` | 服务日志 |

### 3.3 前端（`front/`）

#### 开发模式

```bash
cd front
npm install
npm run dev
```

访问 `http://localhost:5173`。

#### 生产构建

```bash
cd front
npm run build
```

产物在 `front/dist/`，可用任意静态服务器托管（Nginx、Vercel 等）。

#### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/front/dist;
    index index.html;

    # Vue Router hash 模式 — 无需 try_files 配置
    location / {
        # 如改用 history 模式，需加 try_files $uri $uri/ /index.html;
    }

    # 反向代理到 Gateway（生产环境替代 Vite dev proxy）
    location /api/ {
        proxy_pass http://127.0.0.1:7070;
    }
    location /unify-api/ {
        proxy_pass http://127.0.0.1:7070;
    }

    # 反向代理到 Agent
    location /agent-api/ {
        proxy_pass http://127.0.0.1:8000;
        # 需同时修改 front/src/api/agent.js 的 baseURL
    }
}
```

> ⚠️ **生产部署必须修改 `src/api/agent.js` 中的 `baseURL`**。当前硬编码 `http://localhost:8000`，上线后应改为 `/agent-api` 或实际域名。前端 dev proxy **不代理 Agent API**。

---

## 四、完整启动流程（从零开始）

```bash
# ━━━ 1. 基础设施 ━━━
# 确保 MySQL、Nacos 已启动并可访问

# ━━━ 2. 数据库初始化 ━━━
mysql -u<user> -p<pass> -e "CREATE DATABASE IF NOT EXISTS unify_transaction CHARACTER SET utf8mb4;"
mysql -u<user> -p<pass> -e "CREATE DATABASE IF NOT EXISTS unify_news CHARACTER SET utf8mb4;"
mysql -u<user> -p<pass> -e "CREATE DATABASE IF NOT EXISTS unify_memo CHARACTER SET utf8mb4;"
mysql -u<user> -p<pass> -e "CREATE DATABASE IF NOT EXISTS unify_errand CHARACTER SET utf8mb4;"
mysql -u<user> -p<pass> unify_transaction < unify-conv/sql/unify_transaction.sql
mysql -u<user> -p<pass> unify_news < unify-conv/sql/unify_news.sql
mysql -u<user> -p<pass> unify_memo < unify-conv/sql/unify_memo.sql
mysql -u<user> -p<pass> unify_errand < unify-conv/sql/unify_errand.sql

# ━━━ 3. 后端编译 ━━━
cd unify-conv
cd conv-common && mvn install && cd ..
cd conv-api    && mvn install && cd ..
mvn install -DskipTests=true

# ━━━ 4. 后端启动 ━━━
java -jar conv-transaction/target/conv-transaction-1.0.0-SNAPSHOT.jar &
java -jar conv-news/target/conv-news-1.0.0-SNAPSHOT.jar &
java -jar conv-memo/target/conv-memo-1.0.0-SNAPSHOT.jar &
java -jar conv-errand/target/conv-errand-1.0.0-SNAPSHOT.jar &
java -jar conv-gateway/target/conv-gateway-1.0.0-SNAPSHOT.jar &
wait

# ━━━ 5. 智能体 ━━━
cd tian-agent
export DASHSCOPE_API_KEY=sk-your-key-here
pip install -r requirements.txt
python -m uvicorn main:app --host 0.0.0.0 --port 8000 &

# ━━━ 6. 前端 ━━━
cd front
npm install
npm run dev  # 开发环境
# 或 npm run build 后用 Nginx 托管 dist/
```

---

## 五、关键注意事项

### 安全相关

- **`tian-agent/.env` 包含真实 DashScope API Key**，已被提交到 Git。应立即从版本控制中移除（`git rm --cached .env`）。
- **数据库密码硬编码**在 `application.yml`（conv-transaction、conv-news），生产环境务必用环境变量或配置中心覆盖。
- **JWT Secret 硬编码**在 Gateway `application.yml`，生产环境应通过环境变量注入。
- Gateway 免鉴权路径 `/api/**` 暴露了电商网关全部接口，需确认电商侧是否有独立鉴权。

### 架构相关

- **Agent API 不经过 Gateway**：前端直接调用 `localhost:8000`，无统一鉴权层。生产部署时建议将 Agent 也挂到 Gateway 路由下。
- **用户体系外部耦合**：`unify-conv` 不管理用户，所有用户信息通过 HTTP 调用电商网关获取。电商网关不可用时，涉及用户 ID 的业务（交易、备忘、跑腿）将失败。
- **Nacos 是硬依赖**：所有服务启动时必须能连上 Nacos，否则注册失败、Gateway 路由找不到目标服务。

### 数据相关

- `conv-news` 启用了 `@EnableScheduling`，会定时爬取天津大学新闻网站（`news.tju.edu.cn`）。需确保运行环境能访问该网站。
- Chroma 向量库修改分块参数后需删除 `chroma_db/` 和 `md5.text` 重建。
- MySQL 8.x 特性（`ON UPDATE CURRENT_TIMESTAMP`、`DECIMAL`、前缀索引等）使得不能直接替换为 SQLite。如需轻量化开发环境，建议走 Spring Profile 路径另配，而非改写建表 SQL。

### 部署相关

- 项目无 Dockerfile / docker-compose / CI 配置，需自行维护部署流程。
- Maven `skipTests=true` 为默认设置，当前仓库无单元测试。
- 前端仅支持移动端（max-width 480px），桌面端访问体验受限。