package com.study.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
public class TerminalOperationToolTest {

    @Test
    public void testExecuteTerminalCommand() {
        ToolContext context = Mockito.mock(ToolContext.class);
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "dir /b";
        String result = tool.executeTerminalCommand(command, context);
        System.out.println(result);
        assertNotNull(result);
    }
}