package com.study.aiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 过滤 assistant 消息中的思考过程内容（<think>...</think>），
 * 避免推理模型（如 MiniMax-M2.5）的思考文本在工具调用第二次请求时撑爆请求体限制。
 */
public class ThinkingContentFilterAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Pattern THINK_PATTERN = Pattern.compile("(?s)<think>.*?</think>\\s*");

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        return chain.nextCall(filterThinkingContent(request));
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        return chain.nextStream(filterThinkingContent(request));
    }

    private ChatClientRequest filterThinkingContent(ChatClientRequest request) {
        Prompt prompt = request.prompt();
        List<Message> filtered = prompt.getInstructions().stream()
                .map(msg -> {
                    if (msg instanceof AssistantMessage assistantMsg) {
                        String text = assistantMsg.getText();
                        if (text != null && text.contains("<think>")) {
                            String stripped = THINK_PATTERN.matcher(text).replaceAll("").trim();
                            return AssistantMessage.builder()
                                    .content(stripped)
                                    .properties(assistantMsg.getMetadata())
                                    .toolCalls(assistantMsg.getToolCalls())
                                    .media(assistantMsg.getMedia())
                                    .build();
                        }
                    }
                    return msg;
                })
                .toList();
        return request.mutate().prompt(new Prompt(filtered, prompt.getOptions())).build();
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
