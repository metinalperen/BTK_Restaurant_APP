package com.example.demo.exception.analytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when analytics data validation fails
 */
public class AnalyticsValidationException extends RuntimeException {
    
    private final String validationRule;
    private final Object invalidValue;
    private final String fieldName;
    private final List<String> validationErrors;
    
    public AnalyticsValidationException(String validationRule, Object invalidValue, String fieldName) {
        super(String.format("Analytics validation failed. Rule: %s, Field: %s, Value: %s", 
                           validationRule, fieldName, invalidValue));
        this.validationRule = validationRule;
        this.invalidValue = invalidValue;
        this.fieldName = fieldName;
        this.validationErrors = new ArrayList<>();
    }
    
    public AnalyticsValidationException(String validationRule, Object invalidValue, String fieldName, 
                                      List<String> validationErrors) {
        super(String.format("Analytics validation failed. Rule: %s, Field: %s, Value: %s, Errors: %s", 
                           validationRule, fieldName, invalidValue, validationErrors));
        this.validationRule = validationRule;
        this.invalidValue = invalidValue;
        this.fieldName = fieldName;
        this.validationErrors = validationErrors;
    }
    
    // Getters
    public String getValidationRule() { return validationRule; }
    public Object getInvalidValue() { return invalidValue; }
    public String getFieldName() { return fieldName; }
    public List<String> getValidationErrors() { return validationErrors; }
}
