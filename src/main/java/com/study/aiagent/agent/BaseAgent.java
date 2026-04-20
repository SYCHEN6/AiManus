package com.study.aiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.study.aiagent.exception.InvalidParamException;
import com.study.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public abstract class BaseAgent {
    private String name;

    private String systemPrompt;

    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;

    private int maxStep = 10;

    private ChatClient chatClient;

    // 不使用ChatMemory, 自定义维护会化上下文
    private List<Message> messageList = new ArrayList<>();

    // Token 成本限制
    private int tokenLimit = 30000;

    private int currentTokenUsage = 0;

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new InvalidParamException("Failed to run agent, current state = " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new InvalidParamException("Failed to run agent with empty user prompt!");
        }
        // 更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxStep && this.state != AgentState.FINISHED; i++) {
                // 检查 token 限制
                if (currentTokenUsage >= tokenLimit) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Token usage limit exceeded (" + currentTokenUsage + "/" + tokenLimit + ")");
                    break;
                }

                currentStep = i + 1;
                log.info("Executing step {}/{}, token usage: {}/{}", currentStep, maxStep, currentTokenUsage, tokenLimit);
                // 单步执行
                String stepResult = step();
                String result = "Step " +  currentStep + ": " + stepResult;
                results.add(result);
            }
            if (currentStep >= maxStep && this.state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxStep + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("Failed to run agent, exception = {}", e.getMessage());
            return "Agent execute failed";
        } finally {
            this.cleanup();
        }
    }

    /**
     * 定义单个步骤
     *
     * @return 执行结果
     */
    public abstract String step();

    /**
     * 清理资源，子类可按需重写
     */
    protected void cleanup() {
        // 重置 token 计数
        currentTokenUsage = 0;
    }
}
