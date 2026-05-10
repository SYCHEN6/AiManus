package com.study.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

/**
 * 自定义日志advisor拦截器
 * 打印info级别日志，只输出最后一条用户提示词和AI回复文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logRequest(request);
        ChatClientResponse response = chain.nextCall(request);
        logResponse(response);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        logRequest(request);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(
                chain.nextStream(request), this::logResponse);
    }

    private void logRequest(ChatClientRequest request) {
        request.prompt().getInstructions().stream()
                .filter(m -> m instanceof UserMessage)
                .reduce((first, second) -> second)
                .ifPresent(m -> log.info("AI Request: {}", m.getText()));
    }

    private void logResponse(ChatClientResponse response) {
        if (response.chatResponse() != null && response.chatResponse().getResult() != null) {
            log.info("AI Response: {}", response.chatResponse().getResult().getOutput().getText());
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
