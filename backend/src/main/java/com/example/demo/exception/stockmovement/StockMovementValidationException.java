package com.example.demo.exception.stockmovement;
/**
 * 400 - Rezervasyon doğrulama hataları için.
 */
public class StockMovementValidationException extends StockMovementException {
    public StockMovementValidationException(String message) {
        super(message);
    }
}
