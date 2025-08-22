package com.example.demo.exception.analytics;

/**
 * Exception thrown when analytics system runs out of resources
 */
public class AnalyticsResourceExhaustedException extends RuntimeException {
    
    private final String resourceType;
    private final long currentUsage;
    private final long limit;
    private final String operation;
    
    public AnalyticsResourceExhaustedException(String resourceType, long currentUsage, 
                                            long limit, String operation) {
        super(String.format("Analytics resource exhausted: %s. Current: %d, Limit: %d, Operation: %s", 
                           resourceType, currentUsage, limit, operation));
        this.resourceType = resourceType;
        this.currentUsage = currentUsage;
        this.limit = limit;
        this.operation = operation;
    }
    
    // Getters
    public String getResourceType() { return resourceType; }
    public long getCurrentUsage() { return currentUsage; }
    public long getLimit() { return limit; }
    public String getOperation() { return operation; }
}
