package com.study.aiagent.agent;

import com.study.aiagent.advisor.MyLoggerAdvisor;
import com.study.aiagent.agent.multi.AgentAsToolCallback;
import com.study.aiagent.agent.multi.SubAgentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 多 Agent Orchestrator，基于自定义 ReAct 框架（TooCallAgent）。
 *
 * 调用链：
 *   用户 → AiManusOrchestrator（ReAct 循环：think → act → observe）
 *               ↓ act() 触发 AgentAsToolCallback.call()
 *         ResearchAgent / FileAgent / SystemCommAgent（各自独立 ReAct 循环）
 *               ↓ act() 执行对应领域的原始工具
 *         searchWeb / generatePDF / executeTerminalCommand ...
 *
 * Orchestrator 的工具列表只有 3 个子 Agent + terminate，不直接接触任何原始工具。
 */
@Slf4j
@Component
public class AiManusOrchestrator extends TooCallAgent {

    private static final String SYSTEM_PROMPT = """
            You are YuManus, an all-capable AI assistant orchestrator.
            You coordinate three specialized sub-agents to complete complex tasks:
            - callResearchAgent: web search and web page scraping
            - callFileAgent: read/write files, generate/parse PDFs, download resources
            - callSystemAgent: execute terminal commands and send emails
            Delegate each sub-task to the appropriate agent with a clear, self-contained task description.
            Integrate results from multiple agents to provide a comprehensive final answer.
            Always respond in the same language the user uses.
            """;

    private static final String NEXT_STEP_PROMPT = """
            Based on current progress, decide the next step:
            - If more information is needed from the web, call callResearchAgent.
            - If file operations are needed, call callFileAgent.
            - If system commands or emails are needed, call callSystemAgent.
            - If all tasks are complete, call the terminate tool to finish.
            Always provide a detailed, self-contained task description when calling a sub-agent.
            """;

    /**
     * 构造时通过静态工厂方法 buildOrchestratorTools() 先组装好工具列表再传给 super()，
     * 绕开 TooCallAgent.allTools 是 final 的约束（super() 必须是第一条语句）。
     *
     * Orchestrator 只依赖 SubAgentFactory，不直接注入任何原始工具 Bean。
     */
    public AiManusOrchestrator(
            @Qualifier("dashScopeChatModel") ChatModel dashscopeChatModel,
            ToolCallback[] allTools,
            SubAgentFactory subAgentFactory,
            @Value("${agent.token-limit:30000}") int tokenLimit) {

        super(buildOrchestratorTools(allTools, subAgentFactory));

        setName("AiManusOrchestrator");
        setSystemPrompt(SYSTEM_PROMPT);
        setNextStepPrompt(NEXT_STEP_PROMPT);
        setMaxStep(10);
        setTokenLimit(tokenLimit);
        setChatClient(ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build());
    }

    /**
     * 覆写 run()：先执行 ReAct 子 Agent 编排，结束后追加一次纯文本 LLM 调用，
     * 让 Orchestrator 对整个过程做最终总结再返回给用户。
     */
    @Override
    public String run(String userPrompt) {
        super.run(userPrompt);
        return generateFinalSummary();
    }

    /**
     * ReAct 循环结束后，向 LLM 追加一条 UserMessage 请求总结，
     * 不携带任何工具（避免再次触发工具调用），直接返回纯文本。
     */
    private String generateFinalSummary() {
        getMessageList().add(new UserMessage(
                "All sub-tasks have been completed. " +
                "Please provide a concise summary for the user: what was done, " +
                "what files or results were produced, and whether everything succeeded."));

        try {
            ChatResponse response = getChatClient()
                    .prompt(new Prompt(getMessageList()))
                    .system(getSystemPrompt())
                    .call()
                    .chatResponse();

            if (response != null && response.getResult() != null) {
                String summary = response.getResult().getOutput().getText();
                if (summary != null && !summary.isBlank()) {
                    log.info("Final summary generated, length={}", summary.length());
                    return summary;
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate final summary: {}", e.getMessage());
        }
        return "Task completed.";
    }

    /**
     * 静态工厂，作为 super() 的入参（绕开 TooCallAgent.allTools 是 final 的约束）：
     * 1. SubAgentFactory 按分组接口注入各工具 Bean，直接构建子 Agent
     * 2. 将子 Agent 包装为 AgentAsToolCallback 注册给 Orchestrator
     * 3. 从 allTools 中提取 terminate，保证 Orchestrator 的 ReAct 循环能正常终止
     */
    private static ToolCallback[] buildOrchestratorTools(ToolCallback[] allTools, SubAgentFactory factory) {
        List<ToolCallback> tools = new ArrayList<>();

        tools.add(new AgentAsToolCallback(
                "callResearchAgent",
                "Delegate a web research task to Research Agent (searches the web and scrapes web pages)",
                factory.createResearchAgent()));

        tools.add(new AgentAsToolCallback(
                "callFileAgent",
                "Delegate a file operation task to File Agent (read/write files, generate/parse PDF, download resources)",
                factory.createFileAgent()));

        tools.add(new AgentAsToolCallback(
                "callSystemAgent",
                "Delegate a system or communication task to System Agent (execute terminal commands, send emails)",
                factory.createSystemCommAgent()));

        // terminate 工具：Orchestrator 需要它来触发 AgentState.FINISHED
        Arrays.stream(allTools)
                .filter(t -> "doTerminate".equals(t.getToolDefinition().name()))
                .findFirst()
                .ifPresent(tools::add);

        return tools.toArray(new ToolCallback[0]);
    }
}
