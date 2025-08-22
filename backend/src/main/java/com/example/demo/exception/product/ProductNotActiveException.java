package com.example.demo.exception.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductNotActiveException extends ProductException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ProductNotActiveException() {
        super("Product is not active.");
    }

    public ProductNotActiveException(String message) {
        super(message);
    }

    public ProductNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
