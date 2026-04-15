package com.study.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailSendIntegrationTest {

    @Autowired
    private EmailSendTool emailSendTool;

    @Test
    void sendEmail() {
        ToolContext context = Mockito.mock(ToolContext.class);
        String result = emailSendTool.sendEmail(
                "1641717914@qq.com",
                "测试邮件",
                "<h1>Hello</h1><p>这是一封测试邮件</p>",
                context);
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("Email sent successfully"));
    }
}
