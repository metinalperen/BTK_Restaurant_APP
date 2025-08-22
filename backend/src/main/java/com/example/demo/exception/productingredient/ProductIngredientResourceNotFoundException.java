package com.example.demo.exception.productingredient;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductIngredientResourceNotFoundException extends RuntimeException {

    public ProductIngredientResourceNotFoundException() {
        super("Ürün içerik bileşeni bulunamadı.");
    }

    public ProductIngredientResourceNotFoundException(String message) {
        super(message);
    }

    public ProductIngredientResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
