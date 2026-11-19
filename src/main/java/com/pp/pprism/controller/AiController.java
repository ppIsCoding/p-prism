package com.pp.pprism.controller;

import com.pp.pprism.agent.model.PManus;
import com.pp.pprism.app.LoveApp;
import com.pp.pprism.common.BaseResponse;
import com.pp.pprism.common.ModelInfo;
import com.pp.pprism.common.ResultUtils;
import com.pp.pprism.config.ChatModelCatalog;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@Validated
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private ChatModelCatalog chatModelCatalog;

    /**
     * 当前可用的对话模型，前端可据此渲染模型选择器。
     */
    @GetMapping("/models")
    public BaseResponse<ModelInfo> listModels() {
        return ResultUtils.success(new ModelInfo(
                chatModelCatalog.getDefaultModel(),
                chatModelCatalog.getModels()));
    }

    /**
     * 会话 id 缺失时兜底生成，避免记忆组件因 null 抛异常。
     */
    private static String resolveChatId(String chatId) {
        return StringUtils.hasText(chatId) ? chatId : UUID.randomUUID().toString();
    }

    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> doChatWithLoveAppSync(
            @RequestParam("message") @NotBlank(message = "消息不能为空") String message,
            @RequestParam(value = "chatId", required = false) String chatId,
            @RequestParam(value = "model", required = false) String model) {
        return ResultUtils.success(loveApp.doChat(
                message, resolveChatId(chatId), chatModelCatalog.resolve(model)));
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(
            @RequestParam("message") @NotBlank(message = "消息不能为空") String message,
            @RequestParam(value = "chatId", required = false) String chatId,
            @RequestParam(value = "model", required = false) String model) {
        return loveApp.doChatByStream(
                message, resolveChatId(chatId), chatModelCatalog.resolve(model));
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(
            @RequestParam("message") @NotBlank(message = "消息不能为空") String message,
            @RequestParam(value = "chatId", required = false) String chatId,
            @RequestParam(value = "model", required = false) String model) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        loveApp.doChatByStream(message, resolveChatId(chatId), chatModelCatalog.resolve(model))
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message 用户任务
     * @return SSE 事件流
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(
            @RequestParam("message") @NotBlank(message = "消息不能为空") String message) {
        PManus yuManus = new PManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }
}
