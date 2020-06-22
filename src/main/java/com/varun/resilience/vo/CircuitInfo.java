package com.varun.resilience.vo;

public class CircuitInfo {
    private String circuitName;
    private String circuitState;

    public String getCircuitName() {
        return circuitName;
    }

    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
    }

    public String getCircuitState() {
        return circuitState;
    }

    public void setCircuitState(String circuitState) {
        this.circuitState = circuitState;
    }

    public CircuitInfo(String circuitName, String circuitState) {
        this.circuitName = circuitName;
        this.circuitState = circuitState;
    }
}
