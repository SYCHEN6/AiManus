package com.study.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ToolContext;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        ToolContext context = Mockito.mock(ToolContext.class);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.runoob.com/java/java-tutorial.html";
        String result = webScrapingTool.scrapeWebPage(url, context);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}