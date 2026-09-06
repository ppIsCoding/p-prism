package com.pp.pprism.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

 public class WeatherTools {
    @Tool(description = "获取指定城市的当前天气情况")
    String getWeather(@ToolParam(description = "城市名称") String city) {
        // 获取天气的实现逻辑
        return "北京今天晴朗，气温25°C";
    }
}
