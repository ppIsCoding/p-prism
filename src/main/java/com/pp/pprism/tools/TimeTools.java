package com.pp.pprism.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTools {

    @Tool(description = "获取当前系统时间")
    public String getCurrentTime(
            @ToolParam(description = "时间格式，如 yyyy-MM-dd HH:mm:ss，不传则使用默认格式") String format) {
        String pattern = (format == null || format.isBlank()) ? "yyyy-MM-dd HH:mm:ss" : format;
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}