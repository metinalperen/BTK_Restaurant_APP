package com.example.demo.exception.stockmovement;

/**
 * 409 - Conflicts when applying stock movement (e.g., would result in negative stock).
 */
public class StockMovementConflictException extends StockMovementException {
    public StockMovementConflictException(String message) { super(message); }
}

