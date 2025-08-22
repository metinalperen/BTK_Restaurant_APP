package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa rezervasyon ilişkileri ile ilgili fırlatılan exception
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TableReservationConflictException extends DiningTableException {

    public TableReservationConflictException(String message) {
        super(message);
    }

    public static TableReservationConflictException tableHasActiveReservations(Long tableId, int activeReservationCount) {
        return new TableReservationConflictException("Masa üzerinde aktif rezervasyon bulunuyor (ID: " + tableId +
            ", Aktif rezervasyon sayısı: " + activeReservationCount + ")");
    }

    public static TableReservationConflictException cannotChangeStatusWithActiveReservations(Long tableId, String currentStatus, String newStatus) {
        return new TableReservationConflictException("Aktif rezervasyonlu masanın durumu değiştirilemez (ID: " + tableId +
            ", Mevcut: " + currentStatus + ", Yeni: " + newStatus + ")");
    }

    public static TableReservationConflictException reservationExceedsTableCapacity(Long tableId, Integer tableCapacity, Integer guestCount) {
        return new TableReservationConflictException("Rezervasyon masa kapasitesini aşıyor (ID: " + tableId +
            ", Masa kapasitesi: " + tableCapacity + ", Misafir sayısı: " + guestCount + ")");
    }

    public static TableReservationConflictException tableNotAvailableForReservation(Long tableId, String tableStatus) {
        return new TableReservationConflictException("Masa rezervasyon için müsait değil (ID: " + tableId + ", Durum: " + tableStatus + ")");
    }

    public static TableReservationConflictException conflictingReservationTime(Long tableId, String reservationTime) {
        return new TableReservationConflictException("Masa belirtilen saatte zaten rezerve (ID: " + tableId +
            ", Rezervasyon saati: " + reservationTime + ")");
    }

    public static TableReservationConflictException cannotReduceCapacityWithFutureReservations(Long tableId, Integer currentCapacity, Integer newCapacity) {
        return new TableReservationConflictException("Gelecek rezervasyonları olan masanın kapasitesi azaltılamaz (ID: " + tableId +
            ", Mevcut: " + currentCapacity + ", Yeni: " + newCapacity + ")");
    }
}
