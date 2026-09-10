package com.pp.pprism.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;

@Configuration
public class DashScopeChatModelConfig {

    @Bean
    @Primary
    public DashScopeChatModel dashscopeChatModel(@Value("${spring.ai.dashscope.api-key}") String apiKey) {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(new MultimodalRewritingRequestFactory());
        DashScopeApi api = new DashScopeApi("https://dashscope.aliyuncs.com", apiKey, null,
                restClientBuilder, WebClient.builder(), RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel("qwen3.8-27b")
                .withMultiModel(true)
                .build();
        return new DashScopeChatModel(api, options);
    }

    private static class MultimodalRewritingRequestFactory implements ClientHttpRequestFactory {
        private final ClientHttpRequestFactory delegate = new SimpleClientHttpRequestFactory();

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            String url = uri.toString();
            if (url.contains("/text-generation/generation")) {
                uri = URI.create(url.replace("/text-generation/generation", "/multimodal-generation/generation"));
            }
            return delegate.createRequest(uri, httpMethod);
        }
    }
}