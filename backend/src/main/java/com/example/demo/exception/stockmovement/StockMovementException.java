package com.example.demo.exception.stockmovement;

/**
 * Base exception for StockMovement domain errors.
 */
public class StockMovementException extends RuntimeException {
    public StockMovementException(String message) { super(message); }
    public StockMovementException(String message, Throwable cause) { super(message, cause); }
}

