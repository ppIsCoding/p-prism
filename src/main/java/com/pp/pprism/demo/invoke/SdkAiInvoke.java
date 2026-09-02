package com.pp.pprism.demo.invoke;

// 建议dashscope SDK的版本 >= 2.12.0

import java.util.Arrays;
import java.lang.System;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationMessage;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalMessageItemText;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;

public class SdkAiInvoke {

    public static MultiModalConversationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationMessage userMsg = MultiModalConversationMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        new MultiModalMessageItemText("你是谁？")
                ))
                .build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(TestApiKey.API_KEY)
                .model("qwen3.8-27b")
                .messages(Arrays.asList(userMsg))
                .build();
        return conv.call(param);
    }

    public static void main(String[] args) {
        try {
            MultiModalConversationResult result = callWithMessage();
            System.out.println(JsonUtils.toJson(result));
        } catch (ApiException | NoApiKeyException | InputRequiredException | UploadFileException e) {
            // 使用日志框架记录异常信息
            System.err.println("An error occurred while calling the generation service: " + e.getMessage());
        }
        System.exit(0);
    }
}
