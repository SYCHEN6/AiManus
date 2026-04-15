package com.study.aiagent.tools;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

class EmailSendToolTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSendTool emailSendTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 注入 @Value 字段
        ReflectionTestUtils.setField(emailSendTool, "from", "1641717914@qq.com");
        // mock createMimeMessage 返回真实 MimeMessage
        MimeMessage mimeMessage = new MimeMessage((jakarta.mail.Session) null);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail() {
        String account = "1641717914@qq.com";
        String subject = "测试";
        String content = "hello world";
        ToolContext context = Mockito.mock(ToolContext.class);
        String result = emailSendTool.sendEmail(account, subject, content, context);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}
