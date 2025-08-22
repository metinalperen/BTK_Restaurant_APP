package com.example.demo.exception.productingredient;

import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalProductIngredientExceptionHandler {

    // 404 Not Found için özel handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.NOT_FOUND);
    }

    // 409 Conflict için özel handler (örneğin duplicate entity)
    @ExceptionHandler(DuplicateProductIngredientException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateIngredient(DuplicateProductIngredientException ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.CONFLICT);
    }

    // Validation hatalarını yakalar (DTO üzerindeki @Valid anotasyonundan)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                validationErrors.put(err.getField(), err.getDefaultMessage()));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Validation failed",
                request.getDescription(false),
                HttpStatus.BAD_REQUEST.value(),
                validationErrors
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Diğer tüm beklenmeyen hatalar için genel handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllUncaughtExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex.getMessage(), request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Hata cevabı oluşturmayı merkezi yapan yardımcı metot
    private ResponseEntity<ErrorDetails> buildErrorResponse(String message, WebRequest request, HttpStatus status) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                message,
                request.getDescription(false),
                status.value()
        );
        return new ResponseEntity<>(errorDetails, status);
    }

    // Standart hata yanıt formatı
    public static class ErrorDetails {
        private final LocalDateTime timestamp;
        private final String message;
        private final String details;
        private final int status;
        private Map<String, String> validationErrors; // Opsiyonel, validation için

        public ErrorDetails(LocalDateTime timestamp, String message, String details, int status) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
            this.status = status;
        }

        public ErrorDetails(LocalDateTime timestamp, String message, String details, int status, Map<String, String> validationErrors) {
            this(timestamp, message, details, status);
            this.validationErrors = validationErrors;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public int getStatus() { return status; }
        public Map<String, String> getValidationErrors() { return validationErrors; }
    }
}
