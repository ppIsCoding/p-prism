package com.pp.pprism.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";
    // 默认返回结果数量
    private static final int DEFAULT_RESULT_COUNT = 5;

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        // 直接在请求端限制返回数量，减少数据传输和API配额消耗
        paramMap.put("num", DEFAULT_RESULT_COUNT);

        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 检查是否存在错误信息
            if (jsonObject.containsKey("error")) {
                return "Baidu Search API Error: " + jsonObject.getStr("error");
            }

            // 安全提取 organic_results
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "No organic results found for query: " + query;
            }

            // 防止返回结果少于预期数量导致 IndexOutOfBoundsException
            int limit = Math.min(organicResults.size(), DEFAULT_RESULT_COUNT);
            List<Object> objects = organicResults.subList(0, limit);

            // 拼接搜索结果为字符串
            return objects.stream()
                    .map(obj -> ((JSONObject) obj).toString())
                    .collect(Collectors.joining(","));

        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}