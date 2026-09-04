package com.pp.pprism.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Resource
    private VectorStore loveAppVectorStore;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮，我想让另一半（编程导航）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void ragRetrievalWorks() {
        // 直接对知识库做向量检索，验证 RAG 检索链路是否真正生效
        SearchRequest request = SearchRequest.builder()
                .query("我已经结婚了，但是婚后关系不太亲密，怎么办？")
                .topK(5)
                .build();
        List<Document> results = loveAppVectorStore.similaritySearch(request);
        Assertions.assertFalse(results.isEmpty(), "向量检索未返回任何文档，RAG 检索链路可能未生效");
        for (Document doc : results) {
            log.info("retrieved doc [{}]: {}", doc.getMetadata().get("filename"), doc.getText());
        }
        // 检索结果应命中“已婚亲密关系”相关内容
        Assertions.assertTrue(
                results.stream().anyMatch(d -> d.getText().contains("亲密关系")),
                "检索结果中应包含已婚亲密关系相关内容");
    }

    @Test
    void ragAnswerUsesKnowledgeBase() {
        // RAG 答案应引用知识库中的特有内容（如《婚后亲密关系维护秘籍》课程）
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer = loveApp.doChatWithRag(message, chatId);
        log.info("RAG answer: {}", answer);
        Assertions.assertTrue(
                answer.contains("亲密关系维护秘籍") || answer.contains("二人世界") || answer.contains("小惊喜"),
                "RAG 答案应引用知识库特有内容，当前答案可能未真正使用 RAG");
    }

}
