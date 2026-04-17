package com.study.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.study.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
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
        // 调用工具获取结果
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 将工具调用结果拼接到历史上下文后赋值给messageList
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        boolean isTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if (isTerminate) {
            log.info("task finish");
            setState(AgentState.FINISHED);
        }
        String result = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> String.format("tool name: %s, tool result: %s",
                        toolResponse.name(), toolResponse.responseData()))
                .collect(Collectors.joining("\n"));
        log.info("act result is {}", result);
        return result;
    }
}
