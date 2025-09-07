package com.study.aiagent.demo.invoke;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI 框架调用AI
 */
@Component
public class SpringAiInvoke implements CommandLineRunner {

    @Autowired
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好")).getResult().getOutput();
//        System.out.println(assistantMessage.getText());
    }
}
