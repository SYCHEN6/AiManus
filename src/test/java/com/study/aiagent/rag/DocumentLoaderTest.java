package com.study.aiagent.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DocumentLoaderTest {

    @Autowired
    private DocumentLoader documentLoader;

    @Test
    void loadMarkdowns() {
        List<Document> documentList = documentLoader.loadMarkdowns();
        Assertions.assertNotNull(documentList);
    }
}