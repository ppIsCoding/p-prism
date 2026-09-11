package com.pp.pprism.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Slf4j
@Configuration
public class PgVectorVectorStoreConfig {

    private static final String VECTOR_TABLE_NAME = "vector_store";
    private static final String STATE_TABLE_NAME = "vector_store_init_state";

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
    @Resource
    private JdbcTemplate jdbcTemplate;

    private VectorStore vectorStore;

    /**
     * 只构建向量库实例（很快），不做耗时的文档加载与向量化。
     * 否则启动会被 DashScope 调用阻塞，Tomcat 来不及监听端口，
     * 导致微信云托管的存活/就绪探针报 connection refused。
     */
    @Bean
    public VectorStore MyPgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        this.vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName(VECTOR_TABLE_NAME)
                .maxDocumentBatchSize(10000)
                .build();
        return this.vectorStore;
    }

    /**
     * 应用就绪（端口已监听）后再初始化知识库，向量化失败也不影响应用启动。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initVectorStore() {
        try {
            List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
            String currentHash = contentHash(documents);

            // 文档内容未变化且库里已有数据时，跳过关键词增强与向量化，直接复用已有数据
            if (isUpToDate(currentHash)) {
                log.info("知识库文档未变化，跳过关键词增强与向量化（hash={}）", currentHash);
                return;
            }

            // 自动添加元数据
            List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
            // 去重：先删除本次文档文件名对应的历史记录，再重新入库，避免重复启动造成重复数据
            removeStaleDocuments(enrichedDocuments);
            vectorStore.add(enrichedDocuments);
            saveHash(currentHash);
            log.info("知识库文档已变化，完成关键词增强与向量化（hash={}）", currentHash);
        } catch (Exception e) {
            log.error("知识库初始化失败，RAG 功能暂不可用（不影响应用启动）：{}", e.getMessage(), e);
        }
    }

    private boolean isUpToDate(String currentHash) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + STATE_TABLE_NAME + " ("
                + "id integer PRIMARY KEY,"
                + "content_hash text NOT NULL,"
                + "updated_at timestamp NOT NULL DEFAULT now())");

        List<String> hashes = jdbcTemplate.query(
                "SELECT content_hash FROM " + STATE_TABLE_NAME + " WHERE id = 1",
                (rs, rowNum) -> rs.getString(1));
        if (hashes.isEmpty() || !currentHash.equals(hashes.get(0))) {
            return false;
        }

        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + VECTOR_TABLE_NAME, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("查询向量表失败，将重新初始化向量数据：{}", e.getMessage());
            return false;
        }
    }

    private void saveHash(String currentHash) {
        jdbcTemplate.update(
                "INSERT INTO " + STATE_TABLE_NAME + " (id, content_hash, updated_at) VALUES (1, ?, now()) "
                        + "ON CONFLICT (id) DO UPDATE SET content_hash = EXCLUDED.content_hash, updated_at = now()",
                currentHash);
    }

    private String contentHash(List<Document> documents) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Document document : documents) {
                Object filename = document.getMetadata().get("filename");
                digest.update((filename == null ? "" : filename.toString()).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                String text = document.getText();
                digest.update((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("无法计算文档内容指纹", e);
        }
    }

    private void removeStaleDocuments(List<Document> documents) {
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
