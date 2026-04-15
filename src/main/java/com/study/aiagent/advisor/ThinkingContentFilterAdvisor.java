package com.study.aiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 过滤 assistant 消息中的思考过程内容（<think>...</think>），
 * 避免推理模型（如 MiniMax-M2.5）的思考文本在工具调用第二次请求时撑爆请求体限制。
 */
public class ThinkingContentFilterAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final Pattern THINK_PATTERN = Pattern.compile("(?s)<think>.*?</think>\\s*");

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(filterThinkingContent(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(filterThinkingContent(advisedRequest));
    }

    private AdvisedRequest filterThinkingContent(AdvisedRequest request) {
        List<Message> filtered = request.messages().stream()
                .map(msg -> {
                    if (msg instanceof AssistantMessage assistantMsg) {
                        String text = assistantMsg.getText();
                        if (text != null && text.contains("<think>")) {
                            String stripped = THINK_PATTERN.matcher(text).replaceAll("").trim();
                            return new AssistantMessage(stripped, assistantMsg.getMetadata(),
                                    assistantMsg.getToolCalls(), assistantMsg.getMedia());
                        }
                    }
                    return msg;
                })
                .toList();
        return AdvisedRequest.from(request).messages(filtered).build();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE; // 最高优先级，在所有 advisor 之前执行
    }
}
