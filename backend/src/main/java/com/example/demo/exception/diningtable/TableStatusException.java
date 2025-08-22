package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa durumu ile ilgili işlemlerde fırlatılan exception
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TableStatusException extends DiningTableException {

    public TableStatusException(String message) {
        super(message);
    }

    public static TableStatusException invalidStatusTransition(String currentStatus, String newStatus) {
        return new TableStatusException("Geçersiz durum geçişi: " + currentStatus + " -> " + newStatus);
    }

    public static TableStatusException tableNotAvailable(Long tableId, String status) {
        return new TableStatusException("Masa müsait değil (ID: " + tableId + ", Durum: " + status + ")");
    }

    public static TableStatusException cannotOccupyTable(Long tableId, String reason) {
        return new TableStatusException("Masa doldurulamaz (ID: " + tableId + "). Neden: " + reason);
    }

    public static TableStatusException cannotReserveTable(Long tableId, String reason) {
        return new TableStatusException("Masa rezerve edilemez (ID: " + tableId + "). Neden: " + reason);
    }

    public static TableStatusException cannotFreeTable(Long tableId, String reason) {
        return new TableStatusException("Masa boşaltılamaz (ID: " + tableId + "). Neden: " + reason);
    }

    public static TableStatusException statusNotFound(String statusName) {
        return new TableStatusException("Masa durumu bulunamadı: " + statusName);
    }
}
