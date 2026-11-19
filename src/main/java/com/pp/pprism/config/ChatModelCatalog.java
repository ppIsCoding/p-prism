package com.pp.pprism.config;

import com.pp.pprism.exception.BusinessException;
import com.pp.pprism.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 可用对话模型清单，配置在 application.yaml 的 p-prism.ai 下。
 */
@Data
@Component
@ConfigurationProperties(prefix = "p-prism.ai")
public class ChatModelCatalog {

    /**
     * 默认模型；请求未传 model 时使用。
     */
    private String defaultModel = "qwen4-max";

    /**
     * 允许在接口中指定的对话模型。
     */
    private List<String> models = new ArrayList<>();

    /**
     * 保证默认模型一定出现在可选列表中。
     */
    @PostConstruct
    void normalize() {
        if (models == null) {
            models = new ArrayList<>();
        } else {
            models = new ArrayList<>(models);
        }
        if (!StringUtils.hasText(defaultModel)) {
            defaultModel = models.isEmpty() ? "qwen4-max" : models.get(0);
        }
        if (!models.contains(defaultModel)) {
            models.add(0, defaultModel);
        }
    }

    /**
     * 校验并返回本次请求使用的模型。
     */
    public String resolve(String requestedModel) {
        if (!StringUtils.hasText(requestedModel)) {
            return defaultModel;
        }
        if (!models.contains(requestedModel)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的模型: " + requestedModel);
        }
        return requestedModel;
    }
}
