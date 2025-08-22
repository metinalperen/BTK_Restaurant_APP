package com.example.demo.exception.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductAlreadyExistsException extends ProductException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ProductAlreadyExistsException() {
        super("Product already exists.");
    }

    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
