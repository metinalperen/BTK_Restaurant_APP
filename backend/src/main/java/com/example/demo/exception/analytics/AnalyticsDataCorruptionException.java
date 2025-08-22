package com.example.demo.exception.analytics;

/**
 * Exception thrown when analytics data is corrupted or invalid
 */
public class AnalyticsDataCorruptionException extends RuntimeException {
    
    private final String dataType;
    private final String dataValue;
    private final String fieldName;
    
    public AnalyticsDataCorruptionException(String dataType, String fieldName, String dataValue) {
        super(String.format("Analytics data corruption detected in %s field '%s': %s", 
                           dataType, fieldName, dataValue));
        this.dataType = dataType;
        this.fieldName = fieldName;
        this.dataValue = dataValue;
    }
    
    public AnalyticsDataCorruptionException(String dataType, String fieldName, String dataValue, Throwable cause) {
        super(String.format("Analytics data corruption detected in %s field '%s': %s", 
                           dataType, fieldName, dataValue), cause);
        this.dataType = dataType;
        this.fieldName = fieldName;
        this.dataValue = dataValue;
    }
    
    // Getters
    public String getDataType() { return dataType; }
    public String getFieldName() { return fieldName; }
    public String getDataValue() { return dataValue; }
}
