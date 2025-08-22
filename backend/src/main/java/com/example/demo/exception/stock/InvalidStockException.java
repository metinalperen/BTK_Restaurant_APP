package com.example.demo.exception.stock;

public class InvalidStockException extends StockValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidStockException(String message) {
        super(message);
    }

    public InvalidStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
