package com.study.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

/**
 * langchain 访问AI
 */
public class LangChainAiInvoke {
    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("MiniMax-M2.5") // 模型名称
//                .isMultimodalModel(true) // 多模态模型需要手动置为true
                .build();
        String answer = qwenChatModel.chat("你好");
        System.out.println(answer);
    }
}
