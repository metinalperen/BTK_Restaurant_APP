package com.example.demo.exception.analytics;

/**
 * Exception thrown when analytics data is inconsistent
 */
public class AnalyticsDataInconsistencyException extends RuntimeException {
    
    private final String inconsistencyType;
    private final Object expectedValue;
    private final Object actualValue;
    private final String context;
    
    public AnalyticsDataInconsistencyException(String inconsistencyType, Object expectedValue, 
                                            Object actualValue, String context) {
        super(String.format("Analytics data inconsistency detected: %s. Expected: %s, Actual: %s, Context: %s", 
                           inconsistencyType, expectedValue, actualValue, context));
        this.inconsistencyType = inconsistencyType;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        this.context = context;
    }
    
    // Getters
    public String getInconsistencyType() { return inconsistencyType; }
    public Object getExpectedValue() { return expectedValue; }
    public Object getActualValue() { return actualValue; }
    public String getContext() { return context; }
}
