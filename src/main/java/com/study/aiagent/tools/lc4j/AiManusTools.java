package com.study.aiagent.tools.lc4j;

import com.study.aiagent.tools.*;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * LangChain4j 工具适配层。
 *
 * 存在的原因：LangChain4j AiServices 只识别 @dev.langchain4j.agent.tool.Tool 注解，
 * 无法直接使用 Spring AI @Tool 注解的 MyTool bean，因此需要此适配类做注解桥接。
 * 所有实际逻辑均委托给已有的 Spring AI 工具 bean，不重复实现。
 * ToolContext 传 null，因为 LangChain4j 调用链中不存在 Spring AI ToolContext。
 */
@Component
public class AiManusTools {

    @Autowired
    private WebSearchTool webSearchTool;
    @Autowired
    private FileOperationTool fileOperationTool;
    @Autowired
    private WebScrapingTool webScrapingTool;
    @Autowired
    private ResourceDownloadTool resourceDownloadTool;
    @Autowired
    private PDFGenerationTool pdfGenerationTool;
    @Autowired
    private TerminalOperationTool terminalOperationTool;
    @Autowired(required = false)
    private EmailSendTool emailSendTool;

    @Tool("Search for information from Baidu Search Engine")
    public String searchWeb(@P("Search query keyword") String query) {
        return webSearchTool.searchWeb(query, null);
    }

    @Tool("Scrape the content of a web page")
    public String scrapeWebPage(@P("URL of the web page to scrape") String url) {
        return webScrapingTool.scrapeWebPage(url, null);
    }

    @Tool("Read content from a file")
    public String readFile(@P("Name of the file to read") String fileName) {
        return fileOperationTool.readFile(fileName, null);
    }

    @Tool("Write content to a file")
    public String writeFile(
            @P("Name of the file to write") String fileName,
            @P("Content to write to the file") String content) {
        return fileOperationTool.writeFile(fileName, content, null);
    }

    @Tool("Download a resource from a given URL and save it locally")
    public String downloadResource(
            @P("URL of the resource to download") String url,
            @P("File name to save the downloaded resource as") String fileName) {
        return resourceDownloadTool.downloadResource(url, fileName, null);
    }

    @Tool("Generate a PDF file with given text content")
    public String generatePDF(
            @P("File name for the PDF (e.g. report.pdf)") String fileName,
            @P("Text content to include in the PDF") String content) {
        return pdfGenerationTool.doGeneratePdf(fileName, content);
    }

    @Tool("Parse a PDF file and return its text content")
    public String parsePDF(@P("Absolute file path of the PDF to parse") String filePath) {
        return pdfGenerationTool.doParsePdf(filePath);
    }

    @Tool("Execute a shell command in the terminal and return the output")
    public String executeTerminalCommand(@P("Command to execute") String command) {
        return terminalOperationTool.executeTerminalCommand(command, null);
    }

    @Tool("Send an email to the specified recipient")
    public String sendEmail(
            @P("Recipient email address") String to,
            @P("Email subject") String subject,
            @P("Email body content, supports HTML") String content) {
        if (emailSendTool == null) return "Email service not configured";
        return emailSendTool.sendEmail(to, subject, content, null);
    }
}
