package com.study.aiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolRegistration {
    @Autowired
    private List<MyTool> toolList;

    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(toolList.toArray(new Object[0]));
    }
}
