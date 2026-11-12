# AirCookies AI Robot

一个前后端分离的 AI 对话应用，仅作为学习项目，包含 **AI 流式聊天**（支持 DeepSeek 推理过程展示、对话记忆、联网搜索）与 **AI 智能客服**（基于 PGVector 的 RAG 检索增强问答）两大功能模块。

## 技术栈

### 后端（backend/）

| 类别 | 技术 |
| --- | --- |
| 语言 / 运行时 | Java 21 |
| 框架 | Spring Boot、Spring AI（DeepSeek / 兼容模式接入） |
| 向量检索 | Spring AI PGVector（HNSW 索引 + 余弦距离） |
| ORM | MyBatis-Plus（分页插件、p6spy SQL 日志） |
| 数据库 | PostgreSQL（需安装 pgvector 扩展） |
| 联网搜索 | SearXNG（自建元搜索引擎）+ OkHttp + Jsoup 抓取网页正文 |
| 其他 | Log4j2、Lombok、Hutool、Guava、Commons-IO |

### 前端（frontend/）

| 类别 | 技术 |
| --- | --- |
| 框架 | Vue 3 + Vite |
| 状态管理 | Pinia（含持久化插件） |
| UI | Ant Design Vue、Tailwind CSS |
| 流式渲染 | @microsoft/fetch-event-source（SSE）+ markdown-it + highlight.js |
| 路由 | Vue Router（History 模式） |

## 项目结构

```
aircookies-AIrobot
├── backend/                        # Spring Boot 后端
│   └── src/main/java/.../
│       ├── advisor/                # 自定义 Advisor（对话记忆、联网搜索、客服 RAG、流式日志入库）
│       ├── aspect/                 # 接口操作日志切面
│       ├── config/                 # ChatClient、跨域、线程池、OkHttp 等配置
│       ├── controller/             # ChatController（AI 聊天）、AiCustomerServiceController（智能客服）
│       ├── domain/                 # DO 与 Mapper（t_chat / t_chat_message / t_ai_customer_service_md_storage）
│       ├── service/                # 聊天、客服、SearXNG 搜索、网页正文抓取
│       └── reader/                 # Markdown 问答文件解析
└── frontend/
    └── src/
        ├── api/                    # 聊天、客服接口封装
        ├── components/             # 输入框、侧边栏、流式 Markdown 渲染等组件
        ├── stores/                 # Pinia 状态
        └── views/                  # 首页、聊天页、智能客服页
```

## 功能特性

- **AI 聊天**
  - SSE 流式输出，单独展示 DeepSeek 的推理（思考）过程
  - 对话记忆（自定义 `CustomChatMemoryAdvisor`，从数据库加载上下文）
  - 联网搜索（`NetworkSearchAdvisor` → SearXNG 检索 → 抓取网页正文 → 增强提示词）
  - 会话管理:新建、重命名、删除、历史记录分页查询
  - 流式回答自动落库（`CustomStreamLoggerAndMessage2DBAdvisor`）
- **AI 智能客服（RAG）**
  - 上传 Markdown 问答文件，异步解析并向量化写入 PGVector
  - 问答文件的增删改查
  - 检索向量库增强提示词后流式回答

## 快速开始

### 环境要求

- JDK 21
- Node.js `^22.18.0 || >=24.12.0`
- PostgreSQL（已安装 [pgvector](https://pgvector.org/) 扩展）
- SearXNG 服务（仅联网搜索功能需要，默认监听 `http://127.0.0.1:8888/search`）

### 1. 后端配置

项目未提供 `dev/prod` 配置文件（已被 `.gitignore` 排除），需自行创建:

```powershell
cd backend
copy src\main\resources\application-example.yaml src\main\resources\application-dev.yaml
```

按模板填写:数据库连接、DeepSeek `api-key`/`base-url`/模型名、Embedding 模型、SearXNG 地址、客服 Markdown 存储路径等。

同时需准备数据库表(项目暂未提供建表 SQL):

- `t_chat` — 对话记录
- `t_chat_message` — 聊天消息
- `t_ai_customer_service_md_storage` — 客服 Markdown 文件元数据
- `t_vector_store` — 向量表(可由 Spring AI 自动初始化)

### 2. 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

后端默认监听 `8080` 端口。

### 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端开发服务器通过 `/api` 代理转发到 `http://localhost:8080`。

## 已知问题

1. **缺少数据库初始化脚本**:项目暂未提供建表 SQL,pgvector 扩展也需要手动开启。
2. **Vite 代理配置冗余项**:`frontend/vite.config.js` 中 `changeOrigin: true` 拼写正确、代理可用,但 `secure: false` 仅对 https 目标有意义,当前 http 目标下为冗余配置;上线若切换 https 后端需注意证书校验行为。
3. **CORS 配置过于宽松**:`CrossOriginConfig` 对所有路径开放 `allowedOriginPatterns("*")` 且 `allowCredentials(true)`,生产环境应改为白名单域名。
4. **无任何鉴权与限流**:所有接口(包括消耗 API Key 额度的流式对话、Markdown 文件上传/删除)均可匿名调用,公网部署时 Key 会被盗刷。
5. **联网搜索与对话记忆互斥**:`ChatController` 中开启 `networkSearch` 时不再加载 `CustomChatMemoryAdvisor`,联网搜索模式下丢失多轮上下文。
6. **p6spy 生产日志风险**:`spy.properties` 将 SQL 语句及参数值输出到 stdout,生产环境可能泄露业务数据并影响性能,建议按 profile 关闭。
7. **缺少测试覆盖**:仅有应用启动测试和一个 Mapper 测试,核心链路(Advisor、SearXNG、向量化)无单元测试。

## 配置说明

敏感配置(API Key、数据库密码等)一律不要提交到仓库:

- 配置模板:`**/example.yaml`
- 已被忽略:`**/dev.yaml`、`**/prod.yaml`、`**/local.yaml`
- 推荐通过环境变量注入密钥,如 `${DEEPSEEK_API_KEY}`
