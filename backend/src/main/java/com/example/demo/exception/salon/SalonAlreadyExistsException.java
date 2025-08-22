package com.example.demo.exception.salon;

public class SalonAlreadyExistsException extends SalonException {
    public SalonAlreadyExistsException(String message) {
        super(message);
    }
}