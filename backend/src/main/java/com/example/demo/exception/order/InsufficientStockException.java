package com.example.demo.exception.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)  // İstek geçersiz, stok yetersiz ise 400 dönsün
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Yeterli stok bulunmamaktadır.");
    }

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
