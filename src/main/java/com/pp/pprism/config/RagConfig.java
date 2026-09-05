package com.pp.pprism.config;

import com.pp.pprism.rag.LoveAppContextualQueryAugmenterFactory;
import com.pp.pprism.rag.QueryRewriter;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {
    @Bean
    public DocumentRetriever loveAppDocumentRetriever(
            @Qualifier("MyPgVectorVectorStore") VectorStore pgVectorStore){
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(pgVectorStore)  //去pg向量数据库找
                .similarityThreshold(0.5)
                .topK(3)
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor ragAdvisor(
            DocumentRetriever loveAppDocumentRetriever,
            QueryRewriter queryRewriter)
    {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(loveAppDocumentRetriever) //文档检索
                .queryTransformers(queryRewriter.getQueryTransformer()) //查询重写
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance()) //应对rag没查到相关资料的情况
                .build();
    }
}