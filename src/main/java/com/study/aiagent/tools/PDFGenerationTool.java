package com.study.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.study.aiagent.constants.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class PDFGenerationTool implements MyTool {

//    @Tool(description = "Generate a PDF file with given content")
//    public String generatePDF(
//            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
//            @ToolParam(description = "Content to be included in the PDF") String content) {
//        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
//        String filePath = fileDir + "/" + fileName;
//        try {
//            // 创建目录
//            FileUtil.mkdir(fileDir);
//            // 创建 PdfWriter 和 PdfDocument 对象
//            try (PdfWriter writer = new PdfWriter(filePath);
//                 PdfDocument pdf = new PdfDocument(writer);
//                 Document document = new Document(pdf)) {
//                // 自定义字体（需要人工下载字体文件到特定目录）

    /// /                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
    /// /                        .toAbsolutePath().toString();
    /// /                PdfFont font = PdfFontFactory.createFont(fontPath,
    /// /                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
//                // 使用内置中文字体
//                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
//                document.setFont(font);
//                // 创建段落
//                Paragraph paragraph = new Paragraph(content);
//                // 添加段落并关闭文档
//                document.add(paragraph);
//            }
//            return "PDF generated successfully to: " + filePath;
//        } catch (IOException e) {
//            return "Error generating PDF: " + e.getMessage();
//        }
//    }
    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);

            // 创建文档
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 使用内置中文字体支持
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font font = new Font(bfChinese, 12, Font.NORMAL);

            // 添加内容
            Paragraph paragraph = new Paragraph(content, font);
            document.add(paragraph);

            document.close();
            return "PDF generated successfully to: " + filePath;
        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}

