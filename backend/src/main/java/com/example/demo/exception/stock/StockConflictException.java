package com.example.demo.exception.stock;

/**
 * 409 - Conflict in Stock domain (e.g., duplicate name)
 */
public class StockConflictException extends StockException {
    public StockConflictException(String message) { super(message); }
}

