package com.example.demo.exception.productingredient;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateProductIngredientException extends RuntimeException {

    public DuplicateProductIngredientException() {
        super("Ürün içerik bileşeni zaten mevcut.");
    }

    public DuplicateProductIngredientException(String message) {
        super(message);
    }

    public DuplicateProductIngredientException(String message, Throwable cause) {
        super(message, cause);
    }
}
