package com.pp.pprism.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class MyKeywordEnricherTest {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Test
    void testKeywordEnrichment() {
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        log.info("加载到 {} 个文档", documents.size());
        for (Document doc : documents) {
            log.info("原始文档: filename={}, metadata={}", doc.getMetadata().get("filename"), doc.getMetadata());
        }

        List<Document> enriched = myKeywordEnricher.enrichDocuments(documents);
        log.info("富化后共 {} 个文档", enriched.size());
        for (Document doc : enriched) {
            Object keywords = doc.getMetadata().get("excerpt_keywords");
            log.info("filename={}, excerpt_keywords={}", doc.getMetadata().get("filename"), keywords);
            Assertions.assertNotNull(keywords, "关键词元数据不能为空");
            Assertions.assertFalse(String.valueOf(keywords).isBlank(), "关键词元数据不能为空白");
        }
    }
}