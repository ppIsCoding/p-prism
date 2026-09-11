package com.pp.pprism.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

// 已停用：生产使用 MyPgVectorVectorStore（见 PgVectorVectorStoreConfig），
// 此 SimpleVectorStore 未被使用，注释掉以避免启动时重复加载/增强/向量化。
// @Configuration
// public class LoveAppVectorStoreConfig {
//
//     @Resource
//     private LoveAppDocumentLoader loveAppDocumentLoader;
//     @Resource
//     private MyKeywordEnricher  myKeywordEnricher;
//
//     @Bean
//     VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
//         SimpleVectorStore simpleVectorStore = SimpleVectorStore
//                 .builder(dashscopeEmbeddingModel)
//                 .build();
//         // 加载文档
//         List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
//         //自动添加元数据
//         List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
//         simpleVectorStore.add(enrichedDocuments);
//         return simpleVectorStore;
//     }
// }