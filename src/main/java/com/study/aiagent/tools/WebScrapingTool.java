package com.study.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WebScrapingTool implements MyTool{

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url,
                                ToolContext context) {
        System.out.println("chatId = " + context.getContext().get("chatId"));
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // 移除脚本和样式
            doc.select("script, style, meta, link").remove();

            // 提取主要文本内容
            String title = doc.title();
            StringBuilder content = new StringBuilder();
            content.append("标题: ").append(title).append("\n\n");

            // 提取 body 中的文本
            String bodyText = doc.body().text();

            // 限制长度
            int maxLength = 190000;
            if (bodyText.length() > maxLength) {
                bodyText = bodyText.substring(0, maxLength);
                content.append(bodyText).append("\n\n[内容已截断]");
            } else {
                content.append(bodyText);
            }
            return content.toString();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
