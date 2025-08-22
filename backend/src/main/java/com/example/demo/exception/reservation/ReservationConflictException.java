package com.example.demo.exception.reservation;

/**
 * 409 - Çakışan rezervasyon durumlarında fırlatılır.
 */
public class ReservationConflictException extends ReservationException {
    public ReservationConflictException(String message) {
        super(message);
    }
}

