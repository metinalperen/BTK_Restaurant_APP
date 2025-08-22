package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa sipariş ilişkileri ile ilgili fırlatılan exception
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TableOrderConflictException extends DiningTableException {

    public TableOrderConflictException(String message) {
        super(message);
    }

    public static TableOrderConflictException tableHasActiveOrders(Long tableId, int activeOrderCount) {
        return new TableOrderConflictException("Masa üzerinde aktif sipariş bulunuyor (ID: " + tableId +
            ", Aktif sipariş sayısı: " + activeOrderCount + ")");
    }

    public static TableOrderConflictException cannotChangeStatusWithActiveOrders(Long tableId, String currentStatus, String newStatus, int orderCount) {
        return new TableOrderConflictException("Aktif siparişli masanın durumu değiştirilemez (ID: " + tableId +
            ", Mevcut: " + currentStatus + ", Yeni: " + newStatus + ", Sipariş sayısı: " + orderCount + ")");
    }

    public static TableOrderConflictException cannotReduceCapacityWithActiveOrders(Long tableId, Integer currentCapacity, Integer newCapacity) {
        return new TableOrderConflictException("Aktif siparişli masanın kapasitesi azaltılamaz (ID: " + tableId +
            ", Mevcut: " + currentCapacity + ", Yeni: " + newCapacity + ")");
    }

    public static TableOrderConflictException orderExceedsTableCapacity(Long tableId, Integer tableCapacity, Integer requiredCapacity) {
        return new TableOrderConflictException("Sipariş masa kapasitesini aşıyor (ID: " + tableId +
            ", Masa kapasitesi: " + tableCapacity + ", Gerekli kapasite: " + requiredCapacity + ")");
    }

    public static TableOrderConflictException tableNotAvailableForOrder(Long tableId, String tableStatus) {
        return new TableOrderConflictException("Masa sipariş için müsait değil (ID: " + tableId + ", Durum: " + tableStatus + ")");
    }
}
