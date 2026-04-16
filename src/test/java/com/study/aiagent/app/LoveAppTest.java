package com.study.aiagent.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Autowired
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是shaki";
        String answer = loveApp.doChat(message, chatId);
//        message = "我想要另一半（chen）更爱我";
//        answer = loveApp.doChat(message, chatId);
//        message = "我的另一半叫什么呢？帮我回忆一下";
//        answer = loveApp.doChat(message, chatId);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是shaki，我想要另一半（chen）更爱我，但我不知道怎么做";
        LoveApp.LoveReport answer = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRAG() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer = loveApp.doChatWithRAG(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithCloudRAG() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer = loveApp.doChatWithCloudRAG(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");

        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");

//        testMessage("读取tmp/pdf/七夕约会计划.pdf，整理约会详情");

        testMessage("测试给1641717914@qq.com发邮件，主题是约会计划，内容是tmp/pdf/test.pdf");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我找到上海青浦区5公里内合适的约会地点";
        String answer = loveApp.doChatWithMcp(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcpWithImageSearch() {
        String chatId = UUID.randomUUID().toString();
        String message = "帮我搜索好看的约会地点的图片";
        String answer = loveApp.doChatWithMcp(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

}