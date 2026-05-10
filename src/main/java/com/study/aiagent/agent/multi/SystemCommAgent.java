package com.study.aiagent.agent.multi;

import com.study.aiagent.agent.TooCallAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * 专注于系统操作与通信的子 Agent：执行终端命令、发送邮件。
 * 继承 TooCallAgent，拥有独立的 ReAct 循环。
 */
public class SystemCommAgent extends TooCallAgent {

    private static final String SYSTEM_PROMPT = """
            You are a system and communication specialist. Your responsibilities are:
            - Executing terminal/shell commands safely and capturing their output
            - Sending emails to specified recipients with proper subject and content
            Complete the task and report results clearly. When done, stop calling tools and summarize.
            Always respond in the same language the user uses.
            """;

    public SystemCommAgent(ToolCallback[] tools, ChatModel chatModel) {
        super(tools);
        setName("SystemCommAgent");
        setSystemPrompt(SYSTEM_PROMPT);
        setMaxStep(10);
        setChatClient(ChatClient.builder(chatModel).build());
    }
}
