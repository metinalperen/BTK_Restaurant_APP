package com.example.demo.exception.stock;

/**
 * 400 - Validation errors in Stock domain.
 */
public class StockValidationException extends StockException {
    public StockValidationException(String message) { super(message); }
    public StockValidationException(String message, Throwable cause) { super(message, cause); }
}

