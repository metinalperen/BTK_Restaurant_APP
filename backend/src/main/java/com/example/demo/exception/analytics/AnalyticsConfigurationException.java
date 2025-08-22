package com.example.demo.exception.analytics;

/**
 * Exception thrown when analytics configuration is invalid
 */
public class AnalyticsConfigurationException extends RuntimeException {
    
    private final String configKey;
    private final String configValue;
    private final String expectedFormat;
    
    public AnalyticsConfigurationException(String configKey, String configValue, String expectedFormat) {
        super(String.format("Invalid analytics configuration. Key: %s, Value: %s, Expected: %s", 
                           configKey, configValue, expectedFormat));
        this.configKey = configKey;
        this.configValue = configValue;
        this.expectedFormat = expectedFormat;
    }
    
    public AnalyticsConfigurationException(String configKey, String configValue, String expectedFormat, Throwable cause) {
        super(String.format("Invalid analytics configuration. Key: %s, Value: %s, Expected: %s", 
                           configKey, configValue, expectedFormat), cause);
        this.configKey = configKey;
        this.configValue = configValue;
        this.expectedFormat = expectedFormat;
    }
    
    // Getters
    public String getConfigKey() { return configKey; }
    public String getConfigValue() { return configValue; }
    public String getExpectedFormat() { return expectedFormat; }
}
