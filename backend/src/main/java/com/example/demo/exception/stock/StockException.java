package com.example.demo.exception.stock;

/**
 * Base exception for Stock domain errors.
 */
public class StockException extends RuntimeException {
    public StockException(String message) { super(message); }
    public StockException(String message, Throwable cause) { super(message, cause); }
}

