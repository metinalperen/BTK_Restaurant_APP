package com.example.demo.exception.salon;

public class SalonException extends RuntimeException {
    public SalonException(String message) {
        super(message);
    }

    public SalonException(String message, Throwable cause) {
        super(message, cause);
    }

}




