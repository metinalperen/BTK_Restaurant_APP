package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa silme işlemi ile ilgili fırlatılan exception
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TableDeletionException extends DiningTableException {

    public TableDeletionException(String message) {
        super(message);
    }

    public static TableDeletionException cannotDeleteTable(Long tableId, String reason) {
        return new TableDeletionException("Masa silinemez (ID: " + tableId + "). Neden: " + reason);
    }

    public static TableDeletionException tableHasActiveOrders(Long tableId, int orderCount) {
        return new TableDeletionException("Masa silinemez (ID: " + tableId + "). Aktif sipariş sayısı: " + orderCount);
    }

    public static TableDeletionException tableHasActiveReservations(Long tableId, int reservationCount) {
        return new TableDeletionException("Masa silinemez (ID: " + tableId + "). Aktif rezervasyon sayısı: " + reservationCount);
    }

    public static TableDeletionException tableIsOccupied(Long tableId) {
        return new TableDeletionException("Masa silinemez (ID: " + tableId + "). Masa şu anda dolu.");
    }

    public static TableDeletionException tableIsReserved(Long tableId) {
        return new TableDeletionException("Masa silinemez (ID: " + tableId + "). Masa rezerve edilmiş.");
    }
}
