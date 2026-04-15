package com.study.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ToolContext;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {
    FileOperationTool fileOperationTool = new FileOperationTool();
    ToolContext context = Mockito.mock(ToolContext.class);

    @Test
    void readFile() {
        String fileName = "test.md";
        String result = fileOperationTool.readFile(fileName, context);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        String fileName = "test.md";
        String content = "hello world";
        String result = fileOperationTool.writeFile(fileName, content, context);
        Assertions.assertNotNull(result);
    }
}