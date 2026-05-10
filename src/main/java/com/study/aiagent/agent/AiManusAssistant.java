package com.study.aiagent.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j AiServices 接口：定义流式对话入口。
 * AiServices.builder() 会为该接口生成代理，自动完成工具调用循环。
 */
public interface AiManusAssistant {

    @SystemMessage("""
            You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.
            You have various tools at your disposal that you can call upon to efficiently complete complex tasks.
            For complex tasks, break down the problem and use different tools step by step to solve it.
            After using each tool, clearly explain the execution results and suggest the next steps.
            Always respond in the same language the user uses.
            """)
    TokenStream chat(@MemoryId String conversationId, @UserMessage String userMessage);
}
