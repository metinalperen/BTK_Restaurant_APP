package com.example.demo.exception.product;

import java.io.Serial;

/**
 * Base class for all Product-related runtime exceptions.
 */
public class ProductException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }
}

