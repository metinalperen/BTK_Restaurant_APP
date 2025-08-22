package com.example.demo.exception.analytics;

/**
 * Exception thrown when analytics system recovery fails
 */
public class AnalyticsRecoveryException extends RuntimeException {
    
    private final String recoveryStep;
    private final int attemptNumber;
    private final int maxAttempts;
    private final String failureReason;
    
    public AnalyticsRecoveryException(String recoveryStep, int attemptNumber, int maxAttempts, String failureReason) {
        super(String.format("Analytics recovery failed at step: %s. Attempt: %d/%d, Reason: %s", 
                           recoveryStep, attemptNumber, maxAttempts, failureReason));
        this.recoveryStep = recoveryStep;
        this.attemptNumber = attemptNumber;
        this.maxAttempts = maxAttempts;
        this.failureReason = failureReason;
    }
    
    // Getters
    public String getRecoveryStep() { return recoveryStep; }
    public int getAttemptNumber() { return attemptNumber; }
    public int getMaxAttempts() { return maxAttempts; }
    public String getFailureReason() { return failureReason; }
}
