package com.example.demo.exception.reservation;

/**
 * 404 - İstenilen rezervasyon bulunamadığında fırlatılır.
 */
public class ReservationNotFoundException extends ReservationException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}

