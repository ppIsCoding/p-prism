package com.pp.pprism.controller;

import com.pp.pprism.app.LoveApp;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoveApp loveApp;

    @MockitoBean
    private ChatModel dashscopeChatModel;

    @TestConfiguration
    static class StubToolConfig {
        @Bean
        ToolCallback[] allTools() {
            return new ToolCallback[0];
        }
    }

    @Test
    void syncWithoutMessage_shouldReturnParamError() throws Exception {
        mockMvc.perform(get("/ai/love_app/chat/sync"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void syncBlankMessage_shouldReturnParamError() throws Exception {
        mockMvc.perform(get("/ai/love_app/chat/sync").param("message", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void syncWithMessage_shouldReturnSuccessWrapper() throws Exception {
        when(loveApp.doChat(any(), any())).thenReturn("hello");
        mockMvc.perform(get("/ai/love_app/chat/sync").param("message", "hi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data").value("hello"));
    }
}
