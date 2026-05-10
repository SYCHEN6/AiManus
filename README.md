# AI-Agent

基于 **Spring AI 1.1.0 + 阿里云百炼（DashScope）** 构建的多智能体（Multi-Agent）框架，实现了自定义 ReAct（Reason + Act）循环、Orchestrator-SubAgent 分层协作架构，以及丰富的工具调用能力。

---

## 目录

- [项目亮点](#项目亮点)
- [整体架构](#整体架构)
- [模块说明](#模块说明)
- [内置工具](#内置工具)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [使用示例](#使用示例)
- [技术栈](#技术栈)

---

## 项目亮点

- **自定义 ReAct 框架**：`BaseAgent → ReActAgent → TooCallAgent` 三层继承体系，think/act 分离，支持多步推理与工具调用
- **Orchestrator-SubAgent 分层**：顶层 Orchestrator 将任务拆解后分派给 ResearchAgent / FileAgent / SystemCommAgent，各子 Agent 独立完成子任务
- **子 Agent 工具化**：`AgentAsToolCallback` 将子 Agent 包装为标准 `ToolCallback`，Orchestrator 像调用工具一样调用子 Agent
- **Token 用量保护**：每次 ReAct 循环统计 token 消耗，超出配置上限自动终止，防止无限循环
- **工具调用去重**：相同参数的工具调用超过 3 次自动拒绝并提示终止，避免死循环
- **多种 Chat Memory**：内存（InMemory）、文件（FileBasedChatMemery）、Redis（RedisChatMemoryStore）可按需切换
- **RAG 支持**：本地 PGVector 向量库 + 阿里百炼云端知识库，双路 RAG
- **多模型支持**：DashScope（qwen-turbo / MiniMax-M2.5 等）、Ollama 本地模型、LangChain4j 适配层
- **MCP 协议**：通过 `spring-ai-starter-mcp-client` 接入 MCP 工具服务
- **Spring AI 1.1.0**：完整适配新版 Advisor API（CallAdvisor/StreamAdvisor）、builder 模式、toolCallbacks 等

---

## 整体架构

```
用户请求
    │
    ▼
AiManusOrchestrator（ReAct 循环，最多 10 步）
    │  think() → LLM 决策调用哪个子 Agent
    │  act()   → AgentAsToolCallback.call()
    │
    ├─► ResearchAgent（ReAct 循环）
    │       └─ WebSearchTool / WebScrapingTool
    │
    ├─► FileAgent（ReAct 循环）
    │       └─ FileOperationTool / PDFGenerationTool / ResourceDownloadTool
    │
    └─► SystemCommAgent（ReAct 循环）
            └─ TerminalOperationTool / EmailSendTool
```

Orchestrator 结束后自动追加一次无工具调用的 LLM 请求，生成最终汇总回复。

---

## 模块说明

### Agent 层

| 类 | 职责 |
|----|------|
| `BaseAgent` | 状态机（IDLE/RUNNING/FINISHED/ERROR）、ReAct 循环驱动、Token 限制 |
| `ReActAgent` | 定义 `think()` / `act()` 抽象方法，实现 `step()` |
| `TooCallAgent` | 整合 `ToolCallingManager`，驱动工具调用执行，维护工具调用去重缓存 |
| `AiManus` | 单 Agent 实现，直接使用所有工具 |
| `AiManusOrchestrator` | 多 Agent Orchestrator，只持有 3 个子 Agent + terminate |
| `AiManusAssistant` | 基于 Spring AI `AiServices` 的简化 Agent |

### 子 Agent 层（`agent/multi/`）

| 类 | 职责 |
|----|------|
| `SubAgentFactory` | 按工具分组接口自动注入 Bean，构建各子 Agent |
| `AgentAsToolCallback` | 将 `BaseAgent` 包装为 `ToolCallback`，统一入参 schema：`{"task": "..."}` |
| `ResearchAgent` | 网络搜索与网页抓取 |
| `FileAgent` | 文件读写、PDF 生成/解析、资源下载 |
| `SystemCommAgent` | 终端命令执行、邮件发送 |

### Advisor 层

| 类 | 职责 |
|----|------|
| `MyLoggerAdvisor` | 打印请求/响应日志（实现 `CallAdvisor` + `StreamAdvisor`） |
| `ThinkingContentFilterAdvisor` | 过滤 `<think>...</think>` 推理内容，防止二次请求体过大 |
| `ReReadingAdvisor` | Re2 提示词增强，对用户问题二次重复，提升推理准确率 |

### RAG 层

| 类 | 职责 |
|----|------|
| `DocumentLoader` | 读取本地文档并写入 PGVector 向量库 |
| `VectorStoreConfig` | 配置 PGVector VectorStore Bean |
| `RagCloudAdvisorConfig` | 配置阿里百炼云端知识库 Advisor（`RetrievalAugmentationAdvisor`） |

---

## 内置工具

| 工具 | 描述 | 所属分组 |
|------|------|----------|
| `WebSearchTool` | 调用 SearchAPI 搜索百度，返回 Top 5 结果 | ResearchToolGroup |
| `WebScrapingTool` | 使用 Jsoup 抓取网页正文 | ResearchToolGroup |
| `FileOperationTool` | 读取/写入本地文件 | FileToolGroup |
| `PDFGenerationTool` | 生成 PDF（OpenPDF）/ 解析 PDF（PDFBox） | FileToolGroup |
| `ResourceDownloadTool` | 下载远程资源到本地 | FileToolGroup |
| `TerminalOperationTool` | 执行 Shell / CMD 命令，自动识别 OS 编码 | SystemCommToolGroup |
| `EmailSendTool` | 发送 HTML 邮件（Spring Mail，支持 QQ 邮箱） | SystemCommToolGroup |

工具分组接口（`ResearchToolGroup` / `FileToolGroup` / `SystemCommToolGroup`）是标记接口，`SubAgentFactory` 通过 Spring 按接口类型自动收集并分配给对应子 Agent，**新增工具只需实现对应接口，无需修改工厂代码**。

---

## 快速开始

### 环境依赖

- JDK 21
- Maven 3.8+
- Redis（会话记忆，可选）
- PostgreSQL + pgvector 扩展（本地 RAG，可选）

### 克隆与构建

```bash
git clone https://github.com/SYCHEN6/AiManus.git
cd AiManus
mvn compile -q
```

### 配置密钥

在 `src/main/resources/application-local.yml`（或环境变量）中配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: <你的阿里云百炼 API Key>
      chat:
        options:
          model: qwen-turbo   # 也可以用 MiniMax-M2.5 等

search:
  api-key: <你的 SearchAPI Key>   # 网页搜索工具需要

spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: <QQ 邮箱>
    password: <授权码>
```

### 运行测试

```bash
# 单 Agent 测试
mvn test -Dtest=AiManusTest -pl .

# 多 Agent Orchestrator 测试
mvn test -Dtest=AiManusOrchestratorTest -pl .

# LangChain4j Agent 测试
mvn test -Dtest=AiManusLc4jTest -pl .
```

---

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `agent.token-limit` | `30000` | 单次 ReAct 循环 token 消耗上限 |
| `rag.ingest-on-startup` | `false` | 是否在启动时写入本地向量库（首次使用改为 true） |
| `rag.embed-model` | `text-embedding-v3` | 向量化模型名称 |
| `server.port` | `9888` | HTTP 服务端口 |
| `server.servlet.context-path` | `/rest` | API 前缀 |

**Agent 行为**

- 最大步数：`BaseAgent.maxStep = 10`（可在子类构造中覆盖）
- 工具去重：同一工具相同参数调用 ≥ 3 次触发终止
- 子 Agent 每次被 Orchestrator 调用前自动重置状态

---

## 使用示例

### Orchestrator 多 Agent 协作

```java
@Autowired
private AiManusOrchestrator orchestrator;

String result = orchestrator.run(
    "对象在上海青浦区，请帮我找到5公里内合适的约会地点，保存为PDF文件"
);
// Orchestrator 会自动：
// 1. 调用 ResearchAgent 搜索周边地点
// 2. 调用 FileAgent 生成 PDF 并保存
// 3. 汇总结果返回
```

### 单 Agent（AiManus）

```java
@Autowired
private AiManus aiManus;

String result = aiManus.run("搜索今天的AI新闻并写入 news.txt");
```

### 恋爱顾问应用（RAG + Memory + Tools）

```java
@Autowired
private LoveApp loveApp;

// 基础多轮对话
String reply = loveApp.doChat("我最近暗恋一个人，不知道怎么开口", "session-001");

// 带本地向量库的 RAG 问答
String ragReply = loveApp.doChatWithRAG("异地恋怎么维持感情", "session-002");

// 工具调用（搜索 + 文件 + 终端）
String toolReply = loveApp.doChatWithTools("帮我搜索约会技巧并保存到文件", "session-003");
```

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.9 | 基础框架 |
| Spring AI | 1.1.0 | AI 集成核心（ChatClient、Advisor、Tool、RAG） |
| spring-ai-alibaba | 1.1.0.0 | 阿里云百炼 DashScope 接入 |
| LangChain4j | 1.0.0-beta3 | 备用 AI 框架（AiServices、PGVector） |
| PostgreSQL + pgvector | — | 本地 RAG 向量存储 |
| Redis | — | 会话记忆持久化 |
| Jsoup | 1.19.1 | 网页抓取 |
| OpenPDF | 3.0.0 | PDF 生成 |
| PDFBox | 3.0.3 | PDF 解析 |
| Hutool | 5.8.38 | HTTP / JSON 工具 |
| Knife4j | 4.4.0 | API 文档（Swagger UI） |
