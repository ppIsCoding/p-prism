package com.pp.pprism.agent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseAgentLoopTest {

    @Test
    void runShouldStopAfterStepMarksError() {
        FailingAgent agent = new FailingAgent();

        String result = agent.run("test");

        assertEquals(1, agent.stepCount);
        assertEquals(AgentState.ERROR, agent.getState());
        assertTrue(result.contains("boom"));
    }

    private static class FailingAgent extends BaseAgent {

        private int stepCount;

        @Override
        public String step() {
            stepCount++;
            setState(AgentState.ERROR);
            return "boom";
        }
    }
}
