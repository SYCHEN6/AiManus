//package com.study.aiagent.agent;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//class AiManusLc4jTest {
//
//    @Autowired
//    private AiManusLc4j aiManusLc4j;
//
//    /**
//     * 与原 AiManusTest 写法完全一致，入口是 run()。
//     */
//    @Test
//    void run() {
//        String userPrompt = "对象在上海青浦区，" +
//                "请帮我找到5公里内合适的约会地点，" +
//                "保存为PDF文件";
//        String answer = aiManusLc4j.run(userPrompt);
//        System.out.println(answer);
//        Assertions.assertNotNull(answer);
//    }
//
//    /**
//     * 多轮对话：显式传 conversationId，验证 Redis 跨轮记忆。
//     */
//    @Test
//    void runWithSession() {
//        String conversationId = "test-session-001";
//        String answer1 = aiManusLc4j.run("你好，我想找上海青浦区的约会地点", conversationId);
//        System.out.println("Round 1: " + answer1);
//
//        String answer2 = aiManusLc4j.run("帮我把上面的推荐保存成PDF", conversationId);
//        System.out.println("Round 2: " + answer2);
//
//        Assertions.assertNotNull(answer2);
//    }
//}
