package com.pp.pprism.agent.model;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolCallAgentStepTest {

    @Test
    void stepShouldKeepAnswerWhenActAlsoTerminates() {
        ToolCallAgent agent = new ToolCallAgent(new ToolCallback[0]) {
            @Override
            public boolean think() {
                setLastThinkResult("现在是 2026-11-19 16:00:00");
                return true;
            }

            @Override
            public String act() {
                setState(AgentState.FINISHED);
                return "工具 doTerminate 完成了它的任务！结果: \"任务结束\"";
            }
        };

        String result = agent.step();

        assertTrue(result.contains("现在是 2026-11-19 16:00:00"));
        assertTrue(result.contains("doTerminate"));
    }
}
