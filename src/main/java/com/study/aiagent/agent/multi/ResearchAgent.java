package com.study.aiagent.agent.multi;

import com.study.aiagent.agent.TooCallAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * 专注于网络搜索与网页抓取的子 Agent。
 * 继承 TooCallAgent，拥有独立的 ReAct 循环（think → act → observe）。
 */
public class ResearchAgent extends TooCallAgent {

    private static final String SYSTEM_PROMPT = """
            You are a web research specialist. Your only responsibility is finding information from the internet.
            Use web search to locate relevant pages, then scrape them for detailed content as needed.
            When you have gathered enough information, stop calling tools and summarize your findings clearly.
            Always respond in the same language the user uses.
            """;

    public ResearchAgent(ToolCallback[] tools, ChatModel chatModel) {
        super(tools);
        setName("ResearchAgent");
        setSystemPrompt(SYSTEM_PROMPT);
        setMaxStep(10);
        setChatClient(ChatClient.builder(chatModel).build());
    }
}
