package com.example.demo.exception.analytics;

import java.time.Duration;

/**
 * Exception thrown when analytics performance degrades below acceptable thresholds
 */
public class AnalyticsPerformanceDegradationException extends RuntimeException {
    
    private final Duration expectedDuration;
    private final Duration actualDuration;
    private final String operation;
    private final double degradationPercentage;
    
    public AnalyticsPerformanceDegradationException(Duration expectedDuration, Duration actualDuration, 
                                                  String operation) {
        super(String.format("Analytics performance degradation detected. Expected: %s, Actual: %s, Operation: %s", 
                           expectedDuration, actualDuration, operation));
        this.expectedDuration = expectedDuration;
        this.actualDuration = actualDuration;
        this.operation = operation;
        this.degradationPercentage = calculateDegradationPercentage();
    }
    
    private double calculateDegradationPercentage() {
        if (expectedDuration.toMillis() == 0) return 0.0;
        return ((actualDuration.toMillis() - expectedDuration.toMillis()) / (double) expectedDuration.toMillis()) * 100;
    }
    
    // Getters
    public Duration getExpectedDuration() { return expectedDuration; }
    public Duration getActualDuration() { return actualDuration; }
    public String getOperation() { return operation; }
    public double getDegradationPercentage() { return degradationPercentage; }
}
