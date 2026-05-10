package com.study.aiagent.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiManusOrchestratorTest {

    @Autowired
    private AiManusOrchestrator orchestrator;

    @Test
    void run() {
        String userPrompt = "对象在上海青浦区，" +
                "请帮我找到5公里内合适的约会地点，" +
                "保存为PDF文件";
        String answer = orchestrator.run(userPrompt);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }
}
