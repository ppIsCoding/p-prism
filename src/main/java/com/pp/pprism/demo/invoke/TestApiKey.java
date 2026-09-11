package com.pp.pprism.demo.invoke;

public interface TestApiKey {

    /**
     * 请通过环境变量 AI_DASHSCOPE_API_KEY 注入，切勿把真实密钥硬编码进仓库。
     */
    String API_KEY = System.getenv("AI_DASHSCOPE_API_KEY");
}
