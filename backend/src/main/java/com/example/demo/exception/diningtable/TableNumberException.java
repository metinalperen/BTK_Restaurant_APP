package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa numarası ile ilgili işlemlerde fırlatılan exception
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TableNumberException extends DiningTableException {

    public TableNumberException(String message) {
        super(message);
    }

    public static TableNumberException tableNumberAlreadyExists(Integer tableNumber) {
        return new TableNumberException("Bu masa numarası zaten kullanılıyor: " + tableNumber);
    }

    public static TableNumberException tableNumberAlreadyExistsInSalon(Integer tableNumber, String salonName) {
        return new TableNumberException("Bu masa numarası bu salonda zaten kullanılıyor: " + tableNumber + " (Salon: " + salonName + ")");
    }

    public static TableNumberException invalidTableNumber(Integer tableNumber) {
        return new TableNumberException("Geçersiz masa numarası: " + tableNumber + ". Masa numarası 1'den büyük olmalıdır.");
    }

    public static TableNumberException cannotChangeTableNumber(Long tableId, Integer currentNumber, Integer newNumber, String reason) {
        return new TableNumberException("Masa numarası değiştirilemez (ID: " + tableId + ", Mevcut: " + currentNumber + ", Yeni: " + newNumber + "). Neden: " + reason);
    }
}
