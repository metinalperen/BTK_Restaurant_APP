package com.example.demo.exception.stock;

/**
 * 404 - Not Found in Stock domain
 */
public class StockNotFoundException extends StockException {
    public StockNotFoundException(String message) { super(message); }
}

