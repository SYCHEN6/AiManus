package com.study.aiagent.agent.multi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.aiagent.agent.BaseAgent;
import com.study.aiagent.agent.model.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 将子 Agent 包装为 ToolCallback，使 Orchestrator 的 ReAct 循环
 * 可以像调用普通工具一样调用子 Agent。
 *
 * 每次被调用时重置子 Agent 状态，保证子 Agent 可以重复执行。
 */
@Slf4j
public class AgentAsToolCallback implements ToolCallback {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 子 Agent 工具的统一入参 Schema：只有一个 task 字符串
    private static final String INPUT_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "task": {
                  "type": "string",
                  "description": "Detailed task description for the sub-agent to execute"
                }
              },
              "required": ["task"]
            }
            """;

    private final ToolDefinition toolDefinition;
    private final BaseAgent agent;

    public AgentAsToolCallback(String name, String description, BaseAgent agent) {
        this.agent = agent;
        this.toolDefinition = ToolDefinition.builder()
                .name(name)
                .description(description)
                .inputSchema(INPUT_SCHEMA)
                .build();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    /**
     * Orchestrator 调用子 Agent 的入口。
     * toolInput 是 LLM 生成的 JSON，格式：{"task": "..."}
     */
    @Override
    public String call(String toolInput, ToolContext toolContext) {
        return call(toolInput);
    }

    @Override
    public String call(String toolInput) {
        try {
            JsonNode node = MAPPER.readTree(toolInput);
            String task = node.get("task").asText();
            log.info("[AgentTool] {} <- task: {}", toolDefinition.name(), task);

            // 重置子 Agent，使其可以重复被 Orchestrator 调用
            agent.setState(AgentState.IDLE);
            agent.getMessageList().clear();

            String result = agent.run(task);
            log.info("[AgentTool] {} -> result length: {}", toolDefinition.name(), result.length());
            return result;
        } catch (Exception e) {
            log.error("[AgentTool] {} failed: {}", toolDefinition.name(), e.getMessage());
            return "Sub-agent execution failed: " + e.getMessage();
        }
    }
}
