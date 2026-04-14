package com.study.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.runoob.com/wp-content/uploads/2013/12/java.jpg";
        String fileName = "java.jpg";
        String result = tool.downloadResource(url, fileName);
        System.out.println(result);
        assertNotNull(result);
    }
}
