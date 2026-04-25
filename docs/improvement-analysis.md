# TJU-Unify 项目改进分析报告

> 对 `front/`（Vue 3 前端）、`unify-conv/`（Spring Cloud 后端）、`tian-agent/`（AI 智能体）三个子系统的安全、代码质量、架构、可靠性、性能全方位审查。

---

## 一、高危问题（必须优先修复）

### 1.1 安全类

| # | 子系统 | 问题 | 位置 | 说明 |
|---|--------|------|------|------|
| H-01 | Spring | **JWT Secret 硬编码** | `conv-gateway/application.yml:52` | 密钥为可预测的字母数字串，任何人可伪造 JWT 冒充任意用户。应改用环境变量注入，且至少 64 字节随机密钥 |
| H-02 | Spring | **数据库密码硬编码** | `conv-transaction`/`conv-news` `application.yml:11` | `ElmApp@2026!` 明文写死。`conv-memo`/`conv-errand` 已用 `${ENV_VAR:default}` 模式，应统一 |
| H-03 | Spring | **CORS `*` + Credentials** | `SecurityConfig.java:35-39` | `allowOriginPatterns("*")` + `allowCredentials(true)` 允许任意域发起带凭据请求，等效 CSRF 漏洞 |
| H-04 | Spring | **Spring Security 全部 permitAll** | `SecurityConfig.java:24` | `.anyExchange().permitAll()` 使 Security 形同虚设，鉴权完全依赖自定义 Filter，一旦 Filter 有缺陷则全系统裸奔 |
| H-05 | Spring | **username Header 可伪造** | `AuthGlobalFilter.java:78-80` | Gateway 解析 JWT 后将 username 注入普通 HTTP Header，无签名/完整性保护。直连业务服务端口(7771-7774)加 `username` Header 即可绕过全部鉴权 |
| H-06 | Spring | **Token 写入日志** | `UsernameFilter.java:55` | `log.info("设置用户上下文: token={}", token)` — JWT 全文出现在 INFO 日志，可从日志文件窃取 |
| H-07 | Agent | **CORS `*` + Credentials** | `main.py:21-27` | 与 Gateway 同样的问题 |
| H-08 | Agent | **PyYAML FullLoader** | `config_handler.py:7-27` | `yaml.load(..., Loader=yaml.FullLoader)` 允许反序列化任意 Python 对象，应改用 `yaml.safe_load()` |
| H-09 | Agent | **500 错误暴露异常详情** | `main.py:106,158` | `HTTPException(detail=str(e))` 将内部异常栈原样返回客户端，泄露架构信息 |
| H-10 | Agent | **.env 含真实 API Key** | `tian-agent/.env` | `DASHSCOPE_API_KEY=sk-...` 已在 Git 历史中出现。应 `git rm --cached` 移除 |
| H-11 | Front | **硬编码 localhost:8000** | `agent.js:4,42` | 两处硬编码 Agent 地址，生产部署必然失败 |
| H-12 | Front | **window.open 无 noopener** | `Home.vue:377` | 外链缺少 `rel="noopener noreferrer"`，可被 tab-napping 攻击 |

### 1.2 运行时缺陷

| # | 子系统 | 问题 | 位置 | 说明 |
|---|--------|------|------|------|
| H-13 | Spring | **MyBatis `<script>` 写在注解里** | `PostMapper.java:17`、`FileMapper.java:13` | `@Select("<script>...<if>...")` — MyBatis 不会在注解字符串中解析动态 SQL 标签，运行时 `<if>` 条件变成字面文本，查询结果错误 |
| H-14 | Spring | **UserContext.clear() 不清 Token** | `UserContext.java:23-25` | `clear()` 只移除 username ThreadLocal，不移除 token ThreadLocal，JWT 在线程池中泄露到后续请求 |
| H-15 | Spring | **UsernameFilter finally 不调 UserContext.clear()** | `UsernameFilter.java:60-63` | `finally` 只清 MDC，不清 UserContext — 用户身份在线程池中跨请求残留 |
| H-16 | Agent | **内存 Session 无限增长** | `main.py:47` | `sessions: Dict = {}` 无 TTL/清理机制，长时运行内存溢出 |
| H-17 | Agent | **subprocess 安装 pip 包** | `convert_pdfs.py:246-247` | 运行时 `pip install PyPDF2` — 任意代码执行风险 + 网络依赖不可靠 |

---

## 二、中危问题

### 2.1 安全 / 数据

| # | 子系统 | 问题 | 位置 | 说明 |
|---|--------|------|------|------|
| M-01 | Spring | **Nacos 默认密码全部硬编码** | 5 个 `application.yml` | `nacos/nacos` 明文写死，生产应替换并外置 |
| M-02 | Spring | **CSRF 完全关闭** | `SecurityConfig.java:21` | 与 CORS `*`+Credentials 组合，可被跨站请求伪造 |
| M-03 | Spring | **User PO 含 password 字段** | `User.java:22` | 若序列化到 API 响应，密码哈希泄露。应加 `@JsonIgnore` 或拆 DTO |
| M-04 | Spring | **异常消息直接返回客户端** | `GlobalExceptionHandler.java:92-96` | 未处理异常 < 50 字符时原样返回，可暴露 SQL 错误/类名等内部信息 |
| M-05 | Spring | **百度 API Key 在 URL 参数中** | `UnitService.java:44-49` | `client_id=ak&client_secret=sk` 出现在 URL，会记入代理/服务器日志 |
| M-06 | Front | **bearer_token 在请求体而非 Header** | `agent.js:19,40` | JWT 放在 body 字段而非 `Authorization` Header，不符合规范且更易被截取 |
| M-07 | Front | **401 拦截器用 window.location.href** | `request.js:54` | 全页刷新丢 Vue 状态，且跳转路径 `/login` 与其他页面的 `/trade/login` 不一致 |
| M-08 | Front | **登录成功但用户信息获取失败仍设 token** | `auth.js:36-57` | 用户"已登录"但无 userInfo，下游空指针 |
| M-09 | Front | **v-html 渲染助手消息** | `Chat.vue:49` | 已用 DOMPurify 缓解，但 v-html 本质危险，后续改动可能引入 XSS |

### 2.2 代码质量

| # | 子系统 | 问题 | 位置 | 说明 |
|---|--------|------|------|------|
| M-10 | Spring | **N+1 Feign 调用** | `CommentService.java:32-58` | 每条评论单独 HTTP 调用 `userClient.getPersonById()`，50 条评论 = 50 次远程调用。应批量获取 |
| M-11 | Spring | **同一请求内重复 Feign 调用** | `conv-transaction` 多个 Service | `getUserByName(username)` 在同一请求中被多次调用，memo/errand 已抽取 CurrentUserService，transaction 未跟进 |
| M-12 | Spring | **GET 用于状态变更操作** | `PostController` delete、`TransactionRequestController` updateStatus | 浏览器/爬虫预取 GET 会触发删除/状态修改，应改为 DELETE/PUT |
| M-13 | Spring | **7+ 个列表接口无分页** | Contact/Comment/Run/Errand 等多处 | 数据增长后内存溢出 + 响应慢，应统一用 MyBatis-Plus `Page` |
| M-14 | Spring | **无 @Valid 输入校验** | 所有 Controller | Post/Comment/Contact 等实体无 `@NotBlank/@Size` 约束，可提交空/超长数据 |
| M-15 | Spring | **JSON 字符串拼接构造请求体** | `UnitService.java:25-29` | 用户 query 未转义直接拼入 JSON，含引号则 JSON 破损，应使用 ObjectMapper |
| M-16 | Spring | **HttpUtil 原始 HttpURLConnection** | `conv-news HttpUtil.java:32-58` | 无 try-with-resources、无超时/重试、流泄露风险，应换用 RestTemplate |
| M-17 | Spring | **ElmUserClient 用 RestTemplate 不用 Feign** | `ElmUserClient.java:25-28` | URL 硬编码 `bobchasm.cn:8080`，无负载均衡/超时配置，与项目 Feign 架构不一致 |
| M-18 | Spring | **Nacos 注册 IP 硬编码 127.0.0.1** | 4 个服务 `application.yml:22` | 多机部署时服务间无法发现，仅单机开发可用 |
| M-19 | Spring | **isDeleted 类型不一致** | Transaction 用 Boolean、Memo/Errand 用 Integer | JSON 序列化不同（true/false vs 0/1），查询条件写法不同，应统一为 Integer + `@TableLogic` |
| M-20 | Spring | **未启用 @TableLogic 逻辑删除** | 所有实体 | 每个查询需手写 `.eq("is_deleted", 0)`，漏写则返回已删数据。`deleteById` 做物理删除而非逻辑删除 |
| M-21 | Agent | **LLM 调用无超时/重试** | `react_agent.py`、`advanced_retrieval.py` | DashScope API 卡住则 SSE 连接无限挂起 |
| M-22 | Agent | **Agent 流式无迭代上限** | `react_agent.py:152-163` | ReAct 循环无 max_iterations，工具报错重试可无限循环 |
| M-23 | Agent | **get_user_id() 返回随机 ID** | `agent_tools.py:57-59` | 每次调用 `random.choice(user_ids)` 返回不同学生 ID，多步对话身份不一致 |
| M-24 | Agent | **httpx.Client 每次请求新建** | `unify_api_client.py:68` | 无连接池复用，每次 HTTP 调用重建 TCP 连接，延迟浪费 |
| M-25 | Agent | **模块级副作用初始化** | `factory.py:67-68`、`config_handler.py:46-50` | Chat 模型/向量库在 import 时创建，缺 API Key 则整个进程崩溃而非优雅报错 |
| M-26 | Agent | **chat 历史保存异常静默吞掉** | `main.py:98-99,151-152` | `except Exception: pass` — 保存失败无任何日志，历史悄无声息丢失 |
| M-27 | Front | **toast.warning 用于成功消息** | Profile/Memo/Errand/MarketDetail/MarketPublish | 语义错误：发布成功、接单成功等全用 warning 样式 |
| M-28 | Front | **认证跳转路径不一致** | request.js→`/login`，Profile/Market→`/trade/login`，Errand→`/login` | 不同页面跳不同登录页 |
| M-29 | Front | **getUserInfo() 在 setup 外调用** | `MarketDetail.vue:196-198` | 非响应式，token 过期后 userInfo 不更新 |
| M-30 | Front | **native alert/confirm 替代自定义 UI** | Chat.vue 4 处 alert、Memo.vue confirm | 与项目 toast/modal 系统不一致，移动端体验差 |
| M-31 | Front | **API 失败无用户可见反馈** | Market.vue catch 只 console.error | 网络错误用户无感知 |
| M-32 | Front | **路由全部静态导入** | `router/index.js:2-11` | 非首页路由未懒加载，首屏 bundle 臃肿 |
| M-33 | Front | **market.js updateRequestStatus 用 GET** | `market.js:40-41` | 与后端 M-12 对应，状态变更不应是 GET |

### 2.3 性能

| # | 子系统 | 问题 | 位置 | 说明 |
|---|--------|------|------|------|
| M-34 | Agent | **QueryCache 淘汰 O(n)** | `advanced_retrieval.py:52-55` | `min()` 全扫描淘汰最旧缓存项，应改用 `OrderedDict` 或 `lru_cache` |
| M-35 | Agent | **MQE 多路检索串行执行** | `advanced_retrieval.py:306-323` | 5+ 扩展查询逐个顺序检索，可并行化节省数秒 |

---

## 三、低危问题

| # | 子系统 | 问题 | 位置 | 说明 |
|---|--------|------|------|------|
| L-01 | Spring | 空 Feign Client 接口 | `UnifyTransactionClient`/`UnifyNewsClient` | 无方法，空壳死代码 |
| L-02 | Spring | 类名拼写错误 | `TestCofig.java` | 应为 `TestConfig`，且该类为空壳 |
| L-03 | Spring | JPA 注解与 MyBatis-Plus 混用 | `SchoolNews.java`/`Run.java` | `@Id/@Column` 对 MP 无效，误导性 |
| L-04 | Spring | Swagger 安全方案名不一致 | Transaction 用 "token"、Memo/Errand 用 "username" | Gateway 注入的是 username Header，Transaction 的 Swagger Authorize 不可用 |
| L-05 | Spring | PostService.updatePost 废弃 QueryWrapper | `PostService.java:37-39` | 创建了但未使用，疑似遗留代码 |
| L-06 | Agent | 文件名拼写错误 | `vectore_store.py` | 应为 `vector_store.py` |
| L-07 | Agent | HyDE 提示词尾部 `##` | `hyde_prompt.txt:4` | `用户问题：{query}##` 多余字符 |
| L-08 | Agent | MD5 用于去重 | `file_handler.py:27` | 非安全用途但碰撞率高，SHA-256 更稳 |
| L-09 | Agent | datetime 无时区 | `advanced_retrieval.py:36,58,62` | `datetime.now()` 无 UTC，分布式环境 TTL 计算偏差 |
| L-10 | Agent | print() 替代 logger | `factory.py:36-46`、`file_handler.py:18,23,41` | 服务器进程输出到 stdout 不受控 |
| L-11 | Agent | 类型注解错误 | `rag_service.py:16` `_PROMPT_TEXT: str = None`、`file_handler.py:49` `tuple[str]` | 应为 `Optional[str]` 和 `tuple[str, ...]` |
| L-12 | Agent | 死代码工具 | `agent_tools.py:42-54` | `rag_search_mqe/hyde/expanded` 已定义但未注册到 Agent |
| L-13 | Agent | sys.path 多处手动修改 | `react_agent.py:4-7`、`vectore_store.py:4-8`、`main.py:4` | 顺序依赖、fragile |
| L-14 | Front | 重复 scoped `* { margin:0 }` 重置 | Login/Register/MarketDetail/MarketPublish | 与全局 `styles.css` 冲突 |
| L-15 | Front | Toast.vue 用 Options API | `Toast.vue` | 项目其他组件全用 `<script setup>` |
| L-16 | Front | Home.vue 未使用的 ref/import | `request` import、`logoIconUrl` ref、`loading` ref | 死代码 |
| L-17 | Front | Register.vue 无手机号格式校验 | `Register.vue:232` | 只检查非空，不验证格式 |
| L-18 | Front | Memo.vue 死代码函数 | `addCategory`(window.prompt)、`removeCategory`(window.confirm) | 已有自定义模态框替代，native 函数多余 |
| L-19 | Spring | .gitignore 缺少 `__pycache__`、`.venv` | `.gitignore` | Python 产物可能被提交 |
| L-20 | Agent | convert_pdfs.py 页数估算公式 | `convert_pdfs.py:71` | `len(sentences)//20+1` 不准确，应取 PDF 真实页数 |

---

## 四、跨系统架构级改进建议

### 4.1 认证体系重构

当前：Gateway 解 JWT → 提取 username → 注入普通 HTTP Header → 下游盲信

问题：无完整性保护、无服务间认证、Header 可伪造

建议方案（优先级从高到低）：

1. **传递完整 JWT 到下游**：Gateway 不拆 JWT，直接转发；下游服务用共享 `jwt.secret` 自行验证。需每个服务引入 jjwt 依赖
2. **签名 Header**：Gateway 用 HMAC 对 username+timestamp 签名，下游验证签名。轻量但需共享密钥
3. **网络隔离**：生产环境业务端口仅内网可达，Gateway 是唯一外部入口。配合 Nacos IP 动态注册

### 4.2 密钥/密码外置统一方案

当前状态参差：memo/errand 支持环境变量，transaction/news 硬编码

建议：所有服务统一使用 `${ENV_VAR:默认值}` 模式，创建 `.env.example` 文件做模板：

```
# .env.example（不提交，仅作参考）
JWT_SECRET=<随机64字节>
DB_HOST=bobchasm.cn
DB_PORT=3306
DB_USER=elm_app
DB_PASSWORD=******
NACOS_ADDR=bobchasm.cn:8848
NACOS_USER=nacos
NACOS_PASSWORD=******
DASHSCOPE_API_KEY=sk-xxx
```

### 4.3 前端 Agent API 代理化

当前：`agent.js` 硬编码 `http://localhost:8000`，前端直连 Agent

建议：

- 开发环境：在 `vite.config.js` 增加 `/agent-api` proxy 到 `localhost:8000`
- 生产环境：Gateway 增加路由 `/agent-api/**` → Agent 服务
- `agent.js` 改为使用相对路径 `/agent-api`

### 4.4 MyBatis-Plus 逻辑删除标准化

1. 所有实体 `isDeleted` 统一为 `Integer`（0/1）
2. 加 `@TableLogic` 注解 + `@TableField(fill = FieldFill.INSERT)`
3. 注册 `MetaObjectHandler` 自动填充 `isDeleted=0`
4. 全局配置 `mybatis-plus.global-config.db-config.logic-delete-field=isDeleted`

### 4.5 分页统一

1. 所有业务服务注册 `MybatisPlusInterceptor` + `PaginationInnerInterceptor`（当前只有 conv-news 有）
2. 所有列表接口改为接收 `Page` 参数
3. Controller 层统一返回 `IPage<T>` 结构

### 4.6 测试覆盖

当前：零单元测试、零 CI

最低起步：

- Spring：每个 Service 核心方法写 1-2 个 `@SpringBootTest`
- Agent：`pytest` 覆盖 config 加载、session 管理、工具调用
- Front：无测试框架，至少加 `vitest` 做组件渲染测试
- CI：GitHub Actions 做 `mvn compile` + `npm run build` + `pytest`

---

## 五、优先修复路线图

| 阶段 | 周期 | 目标 | 关键项 |
|------|------|------|--------|
| **P0 紧急** | 1-3 天 | 消除安全漏洞 | H-01~H-12：外置密钥、修 CORS、移除 .env from git、修 MyBatis `<script>` 注解、修 UserContext 泄露 |
| **P1 短期** | 1-2 周 | 修复运行时缺陷 | H-13~H-17：Session TTL、LLM 超时/迭代上限、yaml.safe_load、Agent 500 不暴露详情 |
| **P2 中期** | 2-4 周 | 代码质量提升 | M-10~M-35：N+1 消除、统一分页、@Valid 校验、toast 语义、路由懒加载、认证跳转统一 |
| **P3 远期** | 1-2 月 | 架构加固 | 4.1~4.6：认证体系重构、密钥外置统一、Agent 代理化、逻辑删除标准化、测试覆盖起步 |