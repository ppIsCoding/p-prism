package com.pp.pprism.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    public VectorStore MyPgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
//                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        //自动添加元数据
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
        // 去重：先删除本次文档文件名对应的历史记录，再重新入库，避免重复启动造成重复数据
        removeStaleDocuments(vectorStore, enrichedDocuments);
        vectorStore.add(enrichedDocuments);
        return vectorStore;
    }

    private void removeStaleDocuments(VectorStore vectorStore, List<Document> documents) {
        Set<String> filenames = new LinkedHashSet<>();
        for (Document document : documents) {
            Object filename = document.getMetadata().get("filename");
            if (filename != null) {
                filenames.add(String.valueOf(filename));
            }
        }
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        for (String filename : filenames) {
            vectorStore.delete(builder.eq("filename", filename).build());
        }
    }
}

