package com.study.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String fileName = "test.pdf";
        String content = "hello world";
        String result = pdfGenerationTool.generatePDF(fileName, content);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}