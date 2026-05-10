package com.study.aiagent.agent.multi;

import com.study.aiagent.agent.TooCallAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * 专注于文件操作的子 Agent：读写文件、生成/解析 PDF、下载资源。
 * 继承 TooCallAgent，拥有独立的 ReAct 循环。
 */
public class FileAgent extends TooCallAgent {

    private static final String SYSTEM_PROMPT = """
            You are a file system specialist. Your responsibility is handling all file operations:
            reading files, writing files, generating PDFs, parsing PDFs, and downloading remote resources.
            Complete tasks precisely. When done, stop calling tools and report what was done and where files were saved.
            Always respond in the same language the user uses.
            """;

    public FileAgent(ToolCallback[] tools, ChatModel chatModel) {
        super(tools);
        setName("FileAgent");
        setSystemPrompt(SYSTEM_PROMPT);
        setMaxStep(10);
        setChatClient(ChatClient.builder(chatModel).build());
    }
}
