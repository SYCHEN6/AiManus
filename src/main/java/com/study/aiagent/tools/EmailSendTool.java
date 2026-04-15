package com.study.aiagent.tools;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSendTool implements MyTool {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Tool(description = "Send an email to the specified recipient")
    public String sendEmail(
            @ToolParam(description = "Recipient email address") String to,
            @ToolParam(description = "Email subject") String subject,
            @ToolParam(description = "Email body content, supports HTML") String content,
            ToolContext context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            return "Email sent successfully to: " + to;
        } catch (MessagingException e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}
