package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa bulunamadığında fırlatılan exception
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TableNotFoundException extends DiningTableException {

    public TableNotFoundException(Long tableId) {
        super("Masa bulunamadı, ID: " + tableId);
    }

    public TableNotFoundException(Integer tableNumber) {
        super("Masa bulunamadı, Masa Numarası: " + tableNumber);
    }

    public TableNotFoundException(String message) {
        super(message);
    }
}
