package com.study.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {
    @Value("${search.api-key}")
    private String apikey;

//    private WebSearchTool webSearchTool = new WebSearchTool(apikey);

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(apikey);
        String query = "菜鸟教程 https://www.runoob.com/java/java-tutorial.html";
        String result = webSearchTool.searchWeb(query);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}