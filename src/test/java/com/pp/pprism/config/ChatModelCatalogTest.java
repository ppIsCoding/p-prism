package com.pp.pprism.config;

import com.pp.pprism.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatModelCatalogTest {

    private ChatModelCatalog catalog() {
        ChatModelCatalog catalog = new ChatModelCatalog();
        catalog.setDefaultModel("qwen4-max");
        catalog.setModels(List.of("qwen4-max", "qwen3.8-flash"));
        return catalog;
    }

    @Test
    void resolveShouldUseDefaultWhenModelMissing() {
        assertEquals("qwen4-max", catalog().resolve(null));
        assertEquals("qwen4-max", catalog().resolve(" "));
    }

    @Test
    void resolveShouldKeepAvailableModel() {
        assertEquals("qwen3.8-flash", catalog().resolve("qwen3.8-flash"));
    }

    @Test
    void resolveShouldRejectUnknownModel() {
        assertThrows(BusinessException.class, () -> catalog().resolve("not-exists"));
    }

    @Test
    void normalizeShouldKeepDefaultInList() {
        ChatModelCatalog catalog = new ChatModelCatalog();
        catalog.setDefaultModel("qwen4-max");
        catalog.setModels(List.of("qwen3.8-flash"));

        catalog.normalize();

        assertEquals("qwen4-max", catalog.getModels().get(0));
    }
}
