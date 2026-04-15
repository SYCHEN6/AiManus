package com.study.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ToolContext;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {
    ToolContext context = Mockito.mock(ToolContext.class);

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String fileName = "test.pdf";
        String content = "hello world";
        String result = pdfGenerationTool.generatePDF(fileName, content, context);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }

    @Test
    void parserPDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String path = System.getProperty("user.dir") + "/tmp/pdf/七夕约会计划.pdf";
        String result = pdfGenerationTool.parserPDF(path, context);
        Assertions.assertNotNull(result);
    }
}