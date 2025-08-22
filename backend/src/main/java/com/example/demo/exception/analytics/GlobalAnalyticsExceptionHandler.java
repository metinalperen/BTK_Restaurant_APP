package com.example.demo.exception.analytics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for analytics-related exceptions
 */
@ControllerAdvice
@Slf4j
public class GlobalAnalyticsExceptionHandler {

    @ExceptionHandler(SummaryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSummaryNotFoundException(SummaryNotFoundException e) {
        log.warn("Summary not found: {}", e.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Summary Not Found");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("reportDate", e.getReportDate());
        errorResponse.put("reportType", e.getReportType());
        errorResponse.put("period", e.getPeriod());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsGenerationException(AnalyticsGenerationException e) {
        log.error("Analytics generation failed: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Analytics Generation Failed");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("period", e.getPeriod());
        errorResponse.put("startDate", e.getStartDate());
        errorResponse.put("endDate", e.getEndDate());
        errorResponse.put("reportType", e.getReportType());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(SummaryGenerationTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleSummaryGenerationTimeoutException(SummaryGenerationTimeoutException e) {
        log.error("Summary generation timeout: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.REQUEST_TIMEOUT.value());
        errorResponse.put("error", "Summary Generation Timeout");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("timeout", e.getTimeout());
        errorResponse.put("period", e.getPeriod());
        errorResponse.put("startDate", e.getStartDate());
        errorResponse.put("endDate", e.getEndDate());
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsDataCorruptionException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsDataCorruptionException(AnalyticsDataCorruptionException e) {
        log.error("Analytics data corruption detected: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        errorResponse.put("error", "Data Corruption Detected");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("dataType", e.getDataType());
        errorResponse.put("fieldName", e.getFieldName());
        errorResponse.put("dataValue", e.getDataValue());
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsDataInconsistencyException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsDataInconsistencyException(AnalyticsDataInconsistencyException e) {
        log.error("Analytics data inconsistency detected: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Data Inconsistency Detected");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("inconsistencyType", e.getInconsistencyType());
        errorResponse.put("expectedValue", e.getExpectedValue());
        errorResponse.put("actualValue", e.getActualValue());
        errorResponse.put("context", e.getContext());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsResourceExhaustedException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsResourceExhaustedException(AnalyticsResourceExhaustedException e) {
        log.error("Analytics resource exhausted: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Resource Exhausted");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("resourceType", e.getResourceType());
        errorResponse.put("currentUsage", e.getCurrentUsage());
        errorResponse.put("limit", e.getLimit());
        errorResponse.put("operation", e.getOperation());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsPerformanceDegradationException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsPerformanceDegradationException(AnalyticsPerformanceDegradationException e) {
        log.error("Analytics performance degradation: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Analytics Performance Degradation");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsRecoveryException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsRecoveryException(AnalyticsRecoveryException e) {
        log.error("Analytics recovery failed: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Recovery Failed");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("recoveryStep", e.getRecoveryStep());
        errorResponse.put("attemptNumber", e.getAttemptNumber());
        errorResponse.put("maxAttempts", e.getMaxAttempts());
        errorResponse.put("failureReason", e.getFailureReason());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Remove the overly broad Exception.class handler that was catching all exceptions
    // and replace it with more specific analytics-related exception handlers
    
    @ExceptionHandler(AnalyticsConfigurationException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsConfigurationException(AnalyticsConfigurationException e) {
        log.error("Analytics configuration error: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Analytics Configuration Error");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsFallbackException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsFallbackException(AnalyticsFallbackException e) {
        log.error("Analytics fallback error: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Analytics Fallback Error");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(AnalyticsValidationException.class)
    public ResponseEntity<Map<String, Object>> handleAnalyticsValidationException(AnalyticsValidationException e) {
        log.error("Analytics validation error: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Analytics Validation Error");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Only handle analytics-specific exceptions, let other exceptions be handled by the main GlobalExceptionHandler

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(org.springframework.web.servlet.NoHandlerFoundException e) {
        log.warn("No handler found for request: {} {}", e.getHttpMethod(), e.getRequestURL());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Endpoint Not Found");
        errorResponse.put("message", "The requested endpoint does not exist");
        errorResponse.put("method", e.getHttpMethod());
        errorResponse.put("requestURL", e.getRequestURL());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpRequestMethodNotSupportedException(org.springframework.web.HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {} for request", e.getMethod());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        errorResponse.put("error", "Method Not Allowed");
        errorResponse.put("message", "The HTTP method is not supported for this endpoint");
        errorResponse.put("method", e.getMethod());
        errorResponse.put("supportedMethods", e.getSupportedMethods());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(org.springframework.web.bind.MissingServletRequestParameterException e) {
        log.warn("Missing required parameter: {} of type {}", e.getParameterName(), e.getParameterType());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Missing Required Parameter");
        errorResponse.put("message", "A required request parameter is missing");
        errorResponse.put("parameterName", e.getParameterName());
        errorResponse.put("parameterType", e.getParameterType());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
        log.warn("Parameter type mismatch: {} expected {} but got {}", e.getName(), e.getRequiredType(), e.getValue());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Parameter Type Mismatch");
        errorResponse.put("message", "A request parameter has an invalid type");
        errorResponse.put("parameterName", e.getName());
        errorResponse.put("expectedType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown");
        errorResponse.put("actualValue", e.getValue());
        errorResponse.put("exceptionType", e.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
