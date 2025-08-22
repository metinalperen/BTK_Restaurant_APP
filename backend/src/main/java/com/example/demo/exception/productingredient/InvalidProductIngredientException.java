package com.example.demo.exception.productingredient;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProductIngredientException extends RuntimeException {

    public InvalidProductIngredientException() {
        super("Geçersiz ürün içerik bileşeni.");
    }

    public InvalidProductIngredientException(String message) {
        super(message);
    }

    public InvalidProductIngredientException(String message, Throwable cause) {
        super(message, cause);
    }
}
