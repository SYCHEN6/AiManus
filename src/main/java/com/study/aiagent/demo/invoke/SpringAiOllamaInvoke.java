package com.study.aiagent.demo.invoke;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring ai ollama调用AI
 */
@Component
public class SpringAiOllamaInvoke implements CommandLineRunner {

    @Autowired
    private ChatModel ollamaChatModel;

    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = ollamaChatModel.call(new Prompt("你好")).getResult().getOutput();
//        System.out.println(assistantMessage.getText());
    }
}
