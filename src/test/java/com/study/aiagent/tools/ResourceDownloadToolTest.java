package com.study.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ToolContext context = Mockito.mock(ToolContext.class);
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.runoob.com/wp-content/uploads/2013/12/java.jpg";
        String fileName = "java.jpg";
        String result = tool.downloadResource(url, fileName, context);
        System.out.println(result);
        assertNotNull(result);
    }
}
