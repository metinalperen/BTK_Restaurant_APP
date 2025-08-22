# Analytics Exception Handling System

## Overview

This document describes the comprehensive exception handling system for the restaurant analytics platform. The system provides detailed error information, proper HTTP status codes, and graceful fallback mechanisms.

## Exception Hierarchy

All analytics exceptions extend `RuntimeException` and provide detailed context information for debugging and monitoring.

## Exception Classes

### 1. **AnalyticsGenerationException**
**Purpose**: Thrown when analytics summary generation fails

**Context Information**:
- `period`: The time period being processed
- `startDate`: Start date of the reporting period
- `endDate`: End date of the reporting period
- `reportType`: Type of report being generated

**Common Causes**:
- Database connection failures
- Insufficient permissions
- Data corruption during processing
- Resource exhaustion

**HTTP Status**: 500 (Internal Server Error)

**Example**:
```java
throw new AnalyticsGenerationException(
    "Failed to generate summary", 
    TopProductsPeriod.WEEKLY, 
    startDate, 
    endDate, 
    "WEEKLY"
);
```

### 2. **SummaryNotFoundException**
**Purpose**: Thrown when a required analytics summary is not found

**Context Information**:
- `reportDate`: Date for which summary was requested
- `reportType`: Type of report requested
- `period`: Time period requested

**Common Causes**:
- Summary not yet generated
- Invalid date range
- Database inconsistencies

**HTTP Status**: 404 (Not Found)

**Example**:
```java
throw new SummaryNotFoundException(TopProductsPeriod.DAILY, LocalDate.now());
```

### 3. **SummaryGenerationTimeoutException**
**Purpose**: Thrown when summary generation takes too long

**Context Information**:
- `timeout`: Configured timeout duration
- `period`: Time period being processed
- `startDate`: Start date of the reporting period
- `endDate`: End date of the reporting period

**Common Causes**:
- Large dataset processing
- Database performance issues
- System resource constraints

**HTTP Status**: 408 (Request Timeout)

**Example**:
```java
throw new SummaryGenerationTimeoutException(
    Duration.ofMinutes(5), 
    TopProductsPeriod.MONTHLY, 
    startDate, 
    endDate
);
```

### 4. **AnalyticsDataCorruptionException**
**Purpose**: Thrown when analytics data is corrupted or invalid

**Context Information**:
- `dataType`: Type of data that is corrupted
- `fieldName`: Name of the corrupted field
- `dataValue`: The corrupted value

**Common Causes**:
- JSON parsing failures
- Database corruption
- Invalid data formats
- Encoding issues

**HTTP Status**: 422 (Unprocessable Entity)

**Example**:
```java
throw new AnalyticsDataCorruptionException(
    "Top Products JSON", 
    "topProductsJson", 
    corruptedJsonString
);
```

### 5. **AnalyticsDataInconsistencyException**
**Purpose**: Thrown when analytics data is inconsistent

**Context Information**:
- `inconsistencyType`: Type of inconsistency detected
- `expectedValue`: Expected value
- `actualValue`: Actual value found
- `context`: Additional context information

**Common Causes**:
- Data synchronization issues
- Race conditions
- Partial updates
- Cache inconsistencies

**HTTP Status**: 409 (Conflict)

**Example**:
```java
throw new AnalyticsDataInconsistencyException(
    "Revenue Mismatch", 
    expectedRevenue, 
    actualRevenue, 
    "Daily summary calculation"
);
```

### 6. **AnalyticsResourceExhaustedException**
**Purpose**: Thrown when analytics system runs out of resources

**Context Information**:
- `resourceType`: Type of resource exhausted
- `currentUsage`: Current resource usage
- `limit`: Resource limit
- `operation`: Operation being performed

**Common Causes**:
- Memory exhaustion
- Disk space full
- Database connection pool exhausted
- CPU overload

**HTTP Status**: 503 (Service Unavailable)

**Example**:
```java
throw new AnalyticsResourceExhaustedException(
    "Memory", 
    usedMemory, 
    maxMemory, 
    "Summary Generation"
);
```

### 7. **AnalyticsPerformanceDegradationException**
**Purpose**: Thrown when analytics performance degrades below acceptable thresholds

**Context Information**:
- `expectedDuration`: Expected operation duration
- `actualDuration`: Actual operation duration
- `operation`: Operation being performed
- `degradationPercentage`: Calculated degradation percentage

**Common Causes**:
- Database query performance issues
- Large dataset processing
- System resource constraints
- Network latency

**HTTP Status**: 200 (OK) with warning

**Example**:
```java
throw new AnalyticsPerformanceDegradationException(
    Duration.ofMillis(100), 
    Duration.ofMillis(500), 
    "Top Products Query"
);
```

### 8. **AnalyticsConfigurationException**
**Purpose**: Thrown when analytics configuration is invalid

**Context Information**:
- `configKey`: Configuration key that is invalid
- `configValue`: Invalid configuration value
- `expectedFormat`: Expected format or valid values

**Common Causes**:
- Invalid enum values
- Missing required configuration
- Invalid date formats
- Configuration file corruption

**HTTP Status**: 400 (Bad Request)

**Example**:
```java
throw new AnalyticsConfigurationException(
    "period", 
    "INVALID_PERIOD", 
    "DAILY, WEEKLY, MONTHLY, or YEARLY"
);
```

### 9. **AnalyticsValidationException**
**Purpose**: Thrown when analytics data validation fails

**Context Information**:
- `validationRule`: Rule that failed validation
- `fieldName`: Field that failed validation
- `invalidValue`: Value that failed validation
- `validationErrors`: List of specific validation errors

**Common Causes**:
- Invalid data types
- Out-of-range values
- Missing required fields
- Format violations

**HTTP Status**: 400 (Bad Request)

**Example**:
```java
List<String> errors = Arrays.asList("Invalid date format", "Date cannot be in future");
throw new AnalyticsValidationException(
    "Date Range Validation", 
    startDate, 
    "startDate", 
    errors
);
```

### 10. **AnalyticsFallbackException**
**Purpose**: Thrown when analytics fallback mechanism fails

**Context Information**:
- `primaryMethod`: Primary method that failed
- `fallbackMethod`: Fallback method that also failed
- `reason`: Reason for fallback failure

**Common Causes**:
- Both primary and fallback methods failing
- Resource exhaustion in fallback
- Configuration issues in fallback
- Data corruption affecting both methods

**HTTP Status**: 500 (Internal Server Error)

**Example**:
```java
throw new AnalyticsFallbackException(
    "Summary Generation", 
    "Real-time Calculation", 
    "Both methods failed due to database issues"
);
```

### 11. **AnalyticsRecoveryException**
**Purpose**: Thrown when analytics system recovery fails

**Context Information**:
- `recoveryStep`: Step in recovery process that failed
- `attemptNumber`: Current attempt number
- `maxAttempts`: Maximum allowed attempts
- `failureReason`: Reason for recovery failure

**Common Causes**:
- Database connection failures
- Resource exhaustion during recovery
- Configuration issues
- Data corruption preventing recovery

**HTTP Status**: 500 (Internal Server Error)

**Example**:
```java
throw new AnalyticsRecoveryException(
    "Database Connection", 
    3, 
    5, 
    "Connection pool exhausted"
);
```

## Global Exception Handler

The `GlobalAnalyticsExceptionHandler` provides centralized exception handling for all analytics endpoints:

### Features
- **Consistent Error Responses**: All exceptions return structured error responses
- **Proper HTTP Status Codes**: Appropriate status codes for different error types
- **Detailed Logging**: Comprehensive logging for debugging and monitoring
- **Context Preservation**: All relevant context information is included in responses

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Summary Not Found",
  "message": "Summary not found for period: DAILY and date: 2024-01-15",
  "reportDate": "2024-01-15",
  "reportType": "DAILY",
  "period": "DAILY"
}
```

## Best Practices

### 1. **Exception Granularity**
- Use specific exception types for different error scenarios
- Provide detailed context information
- Include relevant metadata for debugging

### 2. **Graceful Degradation**
- Implement fallback mechanisms
- Provide meaningful error messages
- Log errors for monitoring and debugging

### 3. **Resource Management**
- Check resource availability before operations
- Implement timeouts for long-running operations
- Monitor system performance

### 4. **Data Validation**
- Validate input data early
- Check data consistency
- Handle corrupted data gracefully

### 5. **Monitoring and Alerting**
- Log all exceptions with appropriate levels
- Monitor exception frequencies
- Set up alerts for critical failures

## Usage Examples

### In Service Layer
```java
try {
    return getTopProductsFromSummaries(period, endDate, limit);
} catch (SummaryNotFoundException e) {
    log.warn("Summary not found, attempting to generate: {}", e.getMessage());
    generateSummaryIfNeeded(period, endDate);
    return getTopProductsFromSummaries(period, endDate, limit);
} catch (AnalyticsDataCorruptionException e) {
    log.error("Data corruption detected: {}", e.getMessage());
    throw new AnalyticsFallbackException("Summary Data", "Real-time Calculation", 
                                       "Data corruption detected", e);
}
```

### In Controller Layer
```java
@GetMapping("/top-products")
public ResponseEntity<List<TopProductDTO>> getTopProducts(
        @RequestParam TopProductsPeriod period,
        @RequestParam(defaultValue = "10") int limit) {
    
    try {
        List<TopProductDTO> products = analyticsService.getTopProducts(period, limit);
        return ResponseEntity.ok(products);
    } catch (SummaryNotFoundException e) {
        // Global exception handler will process this
        throw e;
    }
}
```

## Configuration

### Timeout Configuration
```properties
# Summary generation timeout (in minutes)
analytics.summary.generation.timeout=5

# Real-time calculation timeout (in milliseconds)
analytics.realtime.timeout=100

# Memory usage threshold (percentage)
analytics.memory.threshold=80
```

### Retry Configuration
```properties
# Maximum recovery attempts
analytics.recovery.max-attempts=5

# Recovery delay between attempts (in seconds)
analytics.recovery.delay=30
```

## Monitoring and Metrics

### Key Metrics to Monitor
- Exception frequency by type
- Response times for different operations
- Resource usage during operations
- Fallback mechanism usage
- Recovery success rates

### Logging Levels
- **ERROR**: System failures, data corruption, resource exhaustion
- **WARN**: Performance degradation, fallback usage, missing data
- **INFO**: Normal operations, summary generation, data updates
- **DEBUG**: Detailed operation information, data parsing details

This exception handling system ensures robust, maintainable, and user-friendly analytics operations with comprehensive error reporting and graceful degradation.
