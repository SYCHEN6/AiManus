package com.study.aiagent.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Autowired
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是shaki";
        String answer = loveApp.doChat(message, chatId);
//        message = "我想要另一半（chen）更爱我";
//        answer = loveApp.doChat(message, chatId);
//        message = "我的另一半叫什么呢？帮我回忆一下";
//        answer = loveApp.doChat(message, chatId);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是shaki，我想要另一半（chen）更爱我，但我不知道怎么做";
        LoveApp.LoveReport answer = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(answer);
    }
}