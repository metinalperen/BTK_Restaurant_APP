package com.example.demo.exception.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProductDataException extends ProductException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidProductDataException() {
        super("Invalid product data provided.");
    }

    public InvalidProductDataException(String message) {
        super(message);
    }

    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
