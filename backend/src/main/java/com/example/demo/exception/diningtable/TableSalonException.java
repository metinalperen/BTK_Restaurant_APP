package com.example.demo.exception.diningtable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Salon ile ilgili masa işlemlerde fırlatılan exception
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TableSalonException extends DiningTableException {

    public TableSalonException(String message) {
        super(message);
    }

    public static TableSalonException salonNotFound(Long salonId) {
        return new TableSalonException("Salon bulunamadı, ID: " + salonId);
    }

    public static TableSalonException salonCapacityExceeded(Long salonId, String salonName, Integer currentCapacity, Integer maxCapacity) {
        return new TableSalonException("Salon kapasitesi aşıldı (ID: " + salonId + ", Salon: " + salonName +
            ", Mevcut: " + currentCapacity + ", Maksimum: " + maxCapacity + ")");
    }

    public static TableSalonException cannotMoveToDifferentSalon(Long tableId, Long currentSalonId, Long newSalonId, String reason) {
        return new TableSalonException("Masa farklı salona taşınamaz (Masa ID: " + tableId +
            ", Mevcut Salon: " + currentSalonId + ", Yeni Salon: " + newSalonId + "). Neden: " + reason);
    }

    public static TableSalonException salonNotActive(Long salonId, String salonName) {
        return new TableSalonException("Salon aktif değil (ID: " + salonId + ", Salon: " + salonName + ")");
    }

    public static TableSalonException tableNumberExistsInTargetSalon(Integer tableNumber, Long targetSalonId, String targetSalonName) {
        return new TableSalonException("Masa numarası hedef salonda zaten mevcut (Masa: " + tableNumber +
            ", Salon ID: " + targetSalonId + ", Salon: " + targetSalonName + ")");
    }
}
