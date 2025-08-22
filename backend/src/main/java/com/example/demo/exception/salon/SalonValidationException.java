package com.example.demo.exception.salon;
/**
 * 400 - Rezervasyon doğrulama hataları için.
 */
public class SalonValidationException extends SalonException {
    public SalonValidationException(String message) {
        super(message);
    }
}
