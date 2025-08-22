package com.example.demo.exception.reservation;

/**
 * Rezervasyon işlemleri için özel exception sınıfı
 */
public class ReservationException extends RuntimeException {

    public ReservationException(String message) {
        super(message);
    }

    public ReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
