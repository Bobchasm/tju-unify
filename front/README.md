# 天大校园助手 - 移动端前端

天津大学校园生活助手移动端前端项目，基于 Vue 3 + Vite 开发。

## 技术栈

- **Vue 3**: 渐进式 JavaScript 框架
- **Vite**: 新一代前端构建工具
- **Vue Router**: Vue.js 官方路由管理器
- **Axios**: HTTP 客户端，用于 API 请求

## 项目结构

```
front/
├── index.html                    # 入口 HTML 文件
├── package.json                  # 项目依赖配置
├── vite.config.js                # Vite 配置文件
├── public/                       # 静态资源目录
└── src/
    ├── main.js                   # Vue 应用入口
    ├── App.vue                   # 根组件
    ├── assets/
    │   └── styles.css            # 全局样式（移动端适配）
    ├── api/
    │   └── agent.js              # 智能体 API 封装
    ├── router/
    │   └── index.js              # 路由配置
    └── views/
        ├── Home.vue              # 首页组件（工具+新闻）
        └── Chat.vue              # 智能体对话组件
```

## 功能模块

### 1. 首页 (Home.vue)

首页包含以下功能：

- **顶部头部**：应用标题和欢迎语
- **常用工具网格**：
  - 🤖 小智助手：跳转到智能体对话页面
  - 🛒 二手市场：待开发功能
  - 💱 交易平台：待开发功能
  - 📋 更多功能：待开发功能
- **校园新闻**：展示校园新闻列表（模拟数据）
- **底部导航栏**：首页、对话、我的

### 2. 智能体对话页 (Chat.vue)

智能体对话页提供以下功能：

- **聊天界面**：类似微信的聊天样式
- **消息历史**：记录用户和助手的完整对话
- **API 集成**：对接 FastAPI 后端服务
- **加载动画**：优雅的等待效果
- **会话管理**：自动维护 session_id
- **消息自动滚动**：新消息自动滚动到底部

## 安装与运行

### 前置要求

- Node.js (推荐 16.x 或更高版本)
- npm 或 yarn 或 pnpm

### 安装依赖

```bash
# 进入前端目录
cd e:\cydd_own_products\tju-unify\front

# 使用 npm 安装依赖
npm install

# 或者使用 yarn
yarn install

# 或者使用 pnpm
pnpm install
```

### 启动开发服务器

```bash
# 开发模式运行
npm run dev

# 打开浏览器访问 http://localhost:5173
```

### 构建生产版本

```bash
# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 移动端适配

项目完全针对移动端设计：

- **最大宽度限制**：480px，适配绝大多数手机屏幕
- **视口设置**：禁用缩放，提供原生应用体验
- **触摸优化**：针对触摸设备优化的交互元素
- **响应式布局**：适应不同尺寸的手机屏幕
- **现代化设计**：使用渐变色和卡片式设计

## API 配置

智能体 API 配置在 `src/api/agent.js` 中：

```javascript
const apiClient = axios.create({
  baseURL: 'http://localhost:8000',  // 后端 API 地址
  timeout: 60000
})
```

如果需要修改后端地址，修改 `baseURL` 即可。

## 后端服务

前端需要配合 FastAPI 后端服务使用：

```bash
# 进入后端目录
cd e:\cydd_own_products\tju-unify\tian-agent

# 启动 FastAPI 服务
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

后端 API 文档访问：http://localhost:8000/docs

## 开发说明

### 路由说明

项目使用 Hash 路由模式，主要路由：

- `/`: 首页
- `/chat`: 智能体对话页

### 添加新页面

1. 在 `src/views/` 目录下创建新的 Vue 组件
2. 在 `src/router/index.js` 中注册路由
3. 在 `Home.vue` 中添加导航入口（如需）

### 添加新 API

1. 在 `src/api/` 目录下创建新的 API 模块
2. 在组件中导入并使用

## 注意事项

1. 确保后端服务在 `http://localhost:8000` 正常运行
2. 开发时建议使用浏览器的开发者工具模拟移动端
3. 生产部署时需要修改 API 地址为实际域名
4. 如遇到跨域问题，需要在后端配置 CORS

## 后续优化方向

- [ ] 接入真实新闻数据 API
- [ ] 实现二手市场和交易平台功能
- [ ] 添加用户登录/注册功能
- [ ] 实现消息推送
- [ ] 接入支付宝/微信支付（如需）
- [ ] 添加更多工具功能（课表、成绩查询等）
- [ ] 优化加载和动画效果
- [ ] 添加国际化支持

## 许可证

请参阅项目根目录的 LICENSE 文件。
