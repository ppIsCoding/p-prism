package com.pp.pprism.common;

import java.util.List;

/**
 * 当前可用的对话模型信息。
 */
public record ModelInfo(String defaultModel, List<String> models) {
}
