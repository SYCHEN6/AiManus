//package com.study.aiagent.agent;
//
//import com.study.aiagent.chatmemory.RedisChatMemoryStore;
//import com.study.aiagent.rag.DocumentLoader;
//import com.study.aiagent.tools.lc4j.AiManusTools;
//import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
//import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.embedding.EmbeddingModel;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.store.embedding.EmbeddingStore;
//import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Flux;
//
//import javax.sql.DataSource;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
///**
// * 基于 LangChain4j 的 AiManus 实现。
// * - 会话记忆：Redis（RedisChatMemoryStore）
// * - RAG 向量库：PGVector（PostgreSQL + pgvector 扩展）
// * - 流式输出：Flux<String>（SSE 友好）
// */
//@Component
//@Slf4j
//public class AiManusLc4j {
//
//    @Value("${spring.ai.dashscope.api-key}")
//    private String apiKey;
//
//    @Value("${spring.ai.dashscope.chat.options.model:qwen-turbo}")
//    private String chatModelName;
//
//    @Value("${rag.embed-model:text-embedding-v3}")
//    private String embedModelName;
//
//    @Value("${rag.ingest-on-startup:false}")
//    private boolean ingestOnStartup;
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Autowired
//    private AiManusTools aiManusTools;
//
//    @Autowired
//    private DocumentLoader documentLoader;
//
//    private AiManusAssistant assistant;
//
//    @PostConstruct
//    public void init() {
//        QwenStreamingChatModel streamingModel = QwenStreamingChatModel.builder()
//                .apiKey(apiKey)
//                .modelName(chatModelName)
//                .build();
//
//        RedisChatMemoryStore memoryStore = new RedisChatMemoryStore(stringRedisTemplate);
//
//        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
//                .apiKey(apiKey)
//                .modelName(embedModelName)
//                .build();
//
//        PgVectorEmbeddingStore embeddingStore = PgVectorEmbeddingStore.datasourceBuilder()
//                .datasource(dataSource)
//                .table("lc4j_embeddings")
//                .dimension(1024)
//                .createTable(true)
//                .build();
//
//        if (ingestOnStartup) {
//            ingestDocuments(embeddingModel, embeddingStore);
//        }
//
//        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(3)
//                .minScore(0.6)
//                .build();
//
//        this.assistant = AiServices.builder(AiManusAssistant.class)
//                .streamingChatLanguageModel(streamingModel)
//                .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
//                        .id(id)
//                        .chatMemoryStore(memoryStore)
//                        .maxMessages(20)
//                        .build())
//                .tools(aiManusTools)
//                .contentRetriever(contentRetriever)
//                .build();
//
//        log.info("AiManusLc4j initialized: model={}, embedModel={}", chatModelName, embedModelName);
//    }
//
//    /**
//     * 与原 AiManus.run() 等价的入口，每次自动生成会话 ID（无跨请求记忆）。
//     */
//    public String run(String userPrompt) {
//        return run(userPrompt, UUID.randomUUID().toString());
//    }
//
//    /**
//     * 带会话 ID 的 run，支持多轮对话（Redis 跨请求持久化）。
//     */
//    public String run(String userPrompt, String conversationId) {
//        return streamChat(conversationId, userPrompt)
//                .collectList()
//                .map(tokens -> String.join("", tokens))
//                .block();
//    }
//
//    /**
//     * 流式对话，返回 Flux<String> 可直接用于 SSE 接口。
//     */
//    public Flux<String> streamChat(String conversationId, String userMessage) {
//        return Flux.create(sink ->
//                assistant.chat(conversationId, userMessage)
//                        .onPartialResponse(sink::next)
//                        .onCompleteResponse(response -> {
//                            log.info("AiManusLc4j conversation={} completed", conversationId);
//                            sink.complete();
//                        })
//                        .onError(sink::error)
//                        .start()
//        );
//    }
//
//    /**
//     * 阻塞式对话，方便测试或无需流式的场景。
//     */
//    public String chat(String conversationId, String userMessage) {
//        return streamChat(conversationId, userMessage)
//                .collectList()
//                .map(tokens -> String.join("", tokens))
//                .block();
//    }
//
//    private void ingestDocuments(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
//        try {
//            var springDocs = documentLoader.loadMarkdowns();
//            List<TextSegment> segments = springDocs.stream()
//                    .map(doc -> TextSegment.from(doc.getText()))
//                    .collect(Collectors.toList());
//
//            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
//            embeddingStore.addAll(embeddings, segments);
//            log.info("Ingested {} document segments into PGVector", segments.size());
//        } catch (Exception e) {
//            log.error("Failed to ingest documents into PGVector", e);
//        }
//    }
//}