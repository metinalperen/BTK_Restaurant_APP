package com.example.demo.exception.reservation;

/**
 * 400 - Rezervasyon doğrulama hataları için.
 */
public class ReservationValidationException extends ReservationException {
    public ReservationValidationException(String message) {
        super(message);
    }
}

