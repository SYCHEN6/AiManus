package com.study.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.study.aiagent.constants.FileConstant;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    @Tool(description = "Generate a PDF file with given content", returnDirect = true)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content,
            ToolContext context) {
        System.out.println("chatId = " + context.getContext().get("chatId"));
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);

            // 创建文档
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 从 classpath 加载字体，避免与系统环境绑定
            byte[] fontBytes;
            try (InputStream fontStream = PDFGenerationTool.class.getResourceAsStream("/font/NotoSansSC-Regular.ttf")) {
                if (fontStream == null) {
                    throw new IOException("字体文件未找到: /font/NotoSansSC-Regular.ttf");
                }
                fontBytes = fontStream.readAllBytes();
            }
            BaseFont bfChinese = BaseFont.createFont("NotoSansSC-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
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

    @Tool(description = "parse pdf, and return content")
    public String parserPDF(@ToolParam(description = "the pdf file path") String filePath, ToolContext context) {
        try {
            validatePathNotEmpty(filePath);
            Path path = validatePathFormat(filePath);
            validateFileExists(path);
            validateFileExtension(filePath);
            validateFileSize(path, 10 * 1024 * 1024); // 10MB限制
            validateFileReadable(path);
            return extractPdfContent(path);
        } catch (IllegalArgumentException | IOException e) {
            return "parse pdf catch an error: " + e.getMessage();
        }
    }

    /**
     * 校验路径不为空
     */
    private static void validatePathNotEmpty(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
    }

    /**
     * 校验路径格式（防止路径遍历攻击）
     */
    private static Path validatePathFormat(String filePath) {
        try {
            // 规范化路径，解析".."等相对路径符号
            return Paths.get(filePath).normalize();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("文件路径格式无效: " + filePath, e);
        }
    }

    /**
     * 校验文件是否存在
     */
    private static void validateFileExists(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在: " + path.toString());
        }
    }

    /**
     * 校验文件扩展名（防止上传恶意文件类型）
     */
    private static void validateFileExtension(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (!lowerPath.endsWith(".pdf")) {
            throw new IllegalArgumentException("文件类型错误，仅支持PDF文件: " + filePath);
        }
    }

    /**
     * 校验文件大小
     * @param path 文件路径
     * @param maxSize 最大允许大小（字节）
     */
    private static void validateFileSize(Path path, long maxSize) throws IOException {
        long fileSize = Files.size(path);
        if (fileSize > maxSize) {
            String sizeMB = String.format("%.2f", fileSize / (1024.0 * 1024.0));
            String maxSizeMB = String.format("%.2f", maxSize / (1024.0 * 1024.0));
            throw new IllegalArgumentException(
                    String.format("文件过大 (%.2f MB)，超过限制 (最大 %s MB)", sizeMB, maxSizeMB)
            );
        }
        if (fileSize == 0) {
            throw new IllegalArgumentException("文件为空，无法解析");
        }
//        System.out.println("文件大小: " + fileSize + " 字节 (" + String.format("%.2f", fileSize / 1024.0) + " KB)");
    }

    /**
     * 校验文件是否可读
     */
    private static void validateFileReadable(Path path) {
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("文件不可读，请检查文件权限: " + path.toString());
        }
    }

    private static String extractPdfContent(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            int pages = document.getNumberOfPages();
            System.out.println("PDF总页数: " + pages);

            int maxPages = 1000;
            if (pages > maxPages) {
                throw new IllegalArgumentException(
                        String.format("PDF页数过多 (%d 页)，超过最大限制 (%d 页)", pages, maxPages)
                );
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            // 限制返回长度，避免超出模型请求体大小限制
            int maxChars = 8000;
            if (text.length() > maxChars) {
                text = text.substring(0, maxChars) + "\n...(内容过长，已截断)";
            }
            System.out.println("pageText = " + text);
            System.out.println("提取字符数: " + text.length());
            return text;
        } catch (Exception e) {
            return "解析PDF文件时发生错误: " + e.getMessage();
        }
    }
}

