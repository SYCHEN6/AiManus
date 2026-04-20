package com.study.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具调用代理类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class TooCallAgent extends ReActAgent{
    private final ToolCallback[] allTools;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    // 记录工具调用次数，防止重复调用相同参数的工具
    private final Map<String, Integer> toolResultCache = new HashMap<>();

    private ChatResponse toolCallChatResponse;

    public TooCallAgent(ToolCallback[] allTools) {
        super();
        this.allTools = allTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                        .withProxyToolCalls(true) // 禁用spring ai内置的工具调用机制，用户自定义维护选项和上下文
                        .build();
    }

    @Override
    public boolean think() {
        try {
            // 1. 校验提示词，拼接用户提示词
            if (StrUtil.isNotBlank(getNextStepPrompt())) {
                UserMessage userMessage = new UserMessage(getNextStepPrompt());
                getMessageList().add(userMessage);
            }
            // 2. 调用AI大模型
            Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(allTools)
                    .toolContext(Map.of("chatId", UUID.randomUUID().toString()))
                    .call()
                    .chatResponse();
            this.toolCallChatResponse = chatResponse;
            if (Objects.isNull(chatResponse) || Objects.isNull(chatResponse.getResult())) {
                log.error("assistantMessage is null or result is null");
                getMessageList().add(new AssistantMessage("chatResponse is empty"));
                return false;
            }
            // 统计 token 消耗
            extractAndUpdateTokenUsage(chatResponse);
            // 3. 解析工具调用结果
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            String result = assistantMessage.getText();
            log.info("assistantMessage output is {}", result);
            if (CollUtil.isEmpty(toolCallList)) {
                getMessageList().add(assistantMessage);
                log.info("assistantMessage return toolCallList is empty");
                return false;
            }
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("tool name: %s, params: %s",
                            toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info("toolCallInfo = {}", toolCallInfo);
            return true;
        } catch (Exception e) {
            log.error("Failed to think, error = {}", e.getMessage());
            getMessageList().add(new AssistantMessage("Agent think failed: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            log.info("need not to call tools");
            return "No tools need to calling";
        }

        List<AssistantMessage.ToolCall> toolCalls = toolCallChatResponse.getResult().getOutput().getToolCalls();
        AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();

        // 检查重复工具
        if (hasRepeatedTools(toolCalls)) {
            return handleRepeatedTools(toolCalls, assistantMessage);
        }

        // 更新缓存
        updateToolCache(toolCalls);

        // 执行工具
        return executeAndHandleTools(assistantMessage);
    }

    private boolean hasRepeatedTools(List<AssistantMessage.ToolCall> toolCalls) {
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            String cacheKey = toolCall.name() + ":" + toolCall.arguments();
            int callCount = toolResultCache.getOrDefault(cacheKey, 0);
            if (callCount >= 3) {
                log.warn("Tool {} has been called {} times, reject this round", toolCall.name(), callCount);
                return true;
            }
        }
        return false;
    }

    private String handleRepeatedTools(List<AssistantMessage.ToolCall> toolCalls, AssistantMessage assistantMessage) {
        getMessageList().add(assistantMessage);
        List<ToolResponseMessage.ToolResponse> toolResponses = buildRepeatedToolResponses(toolCalls);
        ToolResponseMessage virtualResponse = new ToolResponseMessage(toolResponses);
        getMessageList().add(virtualResponse);

        String result = formatToolResponses(toolResponses);
        log.info("Virtual response (repeated tools detected): {}", result);
        return result;
    }

    private List<ToolResponseMessage.ToolResponse> buildRepeatedToolResponses(List<AssistantMessage.ToolCall> toolCalls) {
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            String cacheKey = toolCall.name() + ":" + toolCall.arguments();
            int callCount = toolResultCache.getOrDefault(cacheKey, 0);
            String message = String.format("Tool '%s' has been called %d times with same parameters. Cannot execute. Please use other tools or terminate.",
                    toolCall.name(), callCount);
            responses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), message));
        }
        return responses;
    }

    private void updateToolCache(List<AssistantMessage.ToolCall> toolCalls) {
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            String cacheKey = toolCall.name() + ":" + toolCall.arguments();
            toolResultCache.put(cacheKey, toolResultCache.getOrDefault(cacheKey, 0) + 1);
        }
    }

    private String executeAndHandleTools(AssistantMessage assistantMessage) {
        try {
            ToolExecutionResult toolExecutionResult = executeTools();
            setMessageList(toolExecutionResult.conversationHistory());
            ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
            checkTerminationCondition(toolResponseMessage);
            return formatToolResponses(toolResponseMessage.getResponses());
        } catch (Exception e) {
            log.error("Tool execution failed: {}", e.getMessage());
            return handleToolExecutionError(assistantMessage, e);
        }
    }

    private ToolExecutionResult executeTools() {
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        return toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
    }

    private void checkTerminationCondition(ToolResponseMessage toolResponseMessage) {
        boolean isTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if (isTerminate) {
            log.info("task finish");
            setState(AgentState.FINISHED);
        }
    }

    private String handleToolExecutionError(AssistantMessage assistantMessage, Exception e) {
        List<AssistantMessage.ToolCall> toolCalls = toolCallChatResponse.getResult().getOutput().getToolCalls();
        List<ToolResponseMessage.ToolResponse> errorResponses = new ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            String errorMessage = String.format("Tool '%s' execution failed: %s", toolCall.name(), e.getMessage());
            errorResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), errorMessage));
        }
        ToolResponseMessage errorResponse = new ToolResponseMessage(errorResponses);
        getMessageList().add(assistantMessage);
        getMessageList().add(errorResponse);

        String result = formatToolResponses(errorResponses);
        log.info("Tool execution error response: {}", result);
        return result;
    }

    private String formatToolResponses(List<ToolResponseMessage.ToolResponse> responses) {
        return responses.stream()
                .map(resp -> String.format("tool name: %s, tool result: %s", resp.name(), resp.responseData()))
                .collect(Collectors.joining("\n"));
    }

    private void extractAndUpdateTokenUsage(ChatResponse chatResponse) {
        try {
            Usage tokenUsage = chatResponse.getMetadata().getUsage();
            if (tokenUsage == null) {
                log.error("tokenUsage is null");
                return;
            }

            int tokens = tokenUsage.getTotalTokens();
            setCurrentTokenUsage(getCurrentTokenUsage() + tokens);
            log.info("Token usage: +{}, total: {}/{}", tokens, getCurrentTokenUsage(), getTokenLimit());
        } catch (Exception e) {
            log.error("Failed to extract token usage: {}", e.getMessage());
        }
    }

    @Override
    protected void cleanup() {
        // 清空工具调用缓存，为下一次运行准备
        toolResultCache.clear();
        log.info("Tool result cache cleared");
    }
}
