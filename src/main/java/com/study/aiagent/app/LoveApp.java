package com.study.aiagent.app;

import com.study.aiagent.advisor.MyLoggerAdvisor;
import com.study.aiagent.chatmemory.FileBasedChatMemery;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {
    private final ChatClient chatClient;

    private final static String DEFAULT_SYSTEM = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat_memory";
        ChatMemory chatMemory = new FileBasedChatMemery(fileDir);
//        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(DEFAULT_SYSTEM)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
//                        new ReReadingAdvisor(),
                )
                .build();
    }

    /**
     * AI 基础对话，支持多轮对话记忆
     *
     * @param message message
     * @param chatId chatId
     * @return content
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = Strings.EMPTY;
        if (chatResponse != null) {
            content = chatResponse.getResult().getOutput().getText();
        }
        log.info("content:{}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestionList){}

    /**
     * AI 恋爱报告，支持结构化输出
     *
     * @param message message
     * @param chatId 对话id
     * @return 结果
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport report = chatClient.prompt()
                .user(message)
                .system(DEFAULT_SYSTEM
                        + "每次对话后都要生成恋爱报告，标题为{用户名}的恋爱报告，内容为建议列表")
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("report:{}", report);
        return report;
    }
}
