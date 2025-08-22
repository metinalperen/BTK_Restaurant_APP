package com.example.demo.exception.analytics;

/**
 * Exception thrown when analytics fallback mechanism fails
 */
public class AnalyticsFallbackException extends RuntimeException {
    
    private final String primaryMethod;
    private final String fallbackMethod;
    private final String reason;
    
    public AnalyticsFallbackException(String primaryMethod, String fallbackMethod, String reason) {
        super(String.format("Analytics fallback failed. Primary: %s, Fallback: %s, Reason: %s", 
                           primaryMethod, fallbackMethod, reason));
        this.primaryMethod = primaryMethod;
        this.fallbackMethod = fallbackMethod;
        this.reason = reason;
    }
    
    public AnalyticsFallbackException(String primaryMethod, String fallbackMethod, String reason, Throwable cause) {
        super(String.format("Analytics fallback failed. Primary: %s, Fallback: %s, Reason: %s", 
                           primaryMethod, fallbackMethod, reason), cause);
        this.primaryMethod = primaryMethod;
        this.fallbackMethod = fallbackMethod;
        this.reason = reason;
    }
    
    // Getters
    public String getPrimaryMethod() { return primaryMethod; }
    public String getFallbackMethod() { return fallbackMethod; }
    public String getReason() { return reason; }
}
