package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Masa kapasitesi ile ilgili işlemlerde fırlatılan exception
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TableCapacityException extends DiningTableException {

    public TableCapacityException(String message) {
        super(message);
    }

    public static TableCapacityException invalidCapacity(Integer capacity) {
        return new TableCapacityException("Geçersiz masa kapasitesi: " + capacity + ". Kapasite 1-20 arasında olmalıdır.");
    }

    public static TableCapacityException capacityExceedsLimit(Integer capacity, Integer maxCapacity) {
        return new TableCapacityException("Masa kapasitesi limiti aşıldı: " + capacity + ". Maksimum kapasite: " + maxCapacity);
    }

    public static TableCapacityException capacityBelowMinimum(Integer capacity) {
        return new TableCapacityException("Masa kapasitesi minimum değerin altında: " + capacity + ". Minimum kapasite: 1");
    }

    public static TableCapacityException cannotReduceCapacity(Long tableId, Integer currentCapacity, Integer newCapacity, String reason) {
        return new TableCapacityException("Masa kapasitesi azaltılamaz (ID: " + tableId + ", Mevcut: " + currentCapacity + ", Yeni: " + newCapacity + "). Neden: " + reason);
    }

    public static TableCapacityException insufficientCapacity(Long tableId, Integer tableCapacity, Integer requiredCapacity) {
        return new TableCapacityException("Masa kapasitesi yetersiz (ID: " + tableId + ", Kapasite: " + tableCapacity + ", Gerekli: " + requiredCapacity + ")");
    }
}
