package com.example.demo.validation;

import com.example.demo.dto.request.DiningTableRequestDto;
import com.example.demo.exception.diningtable.*;
import com.example.demo.model.DiningTable;
import com.example.demo.model.TableStatus;
import com.example.demo.repository.DiningTableRepository;

import java.util.Optional;

/**
 * DiningTable (Masa) validasyon işlemlerini yöneten sınıf
 */
public class DiningTableValidator {

    public static void validateCreateDiningTable(DiningTableRequestDto diningTableDto, DiningTableRepository diningTableRepository) {
        validateBasicFields(diningTableDto);

        // Masa numarası benzersizlik kontrolü
        Optional<DiningTable> existingTable = diningTableRepository.findByTableNumber(diningTableDto.getTableNumber());
        if (existingTable.isPresent()) {
            throw TableNumberException.tableNumberAlreadyExists(diningTableDto.getTableNumber());
        }
    }

    public static void validateUpdateDiningTable(Long id, DiningTableRequestDto diningTableDto, DiningTableRepository diningTableRepository) {
        validateBasicFields(diningTableDto);

        DiningTable currentTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new TableNotFoundException(id));

        // Masa numarası değişikliği kontrolü
        if (!currentTable.getTableNumber().equals(diningTableDto.getTableNumber())) {
            Optional<DiningTable> tableWithSameNumber = diningTableRepository.findByTableNumber(diningTableDto.getTableNumber());
            if (tableWithSameNumber.isPresent() && !tableWithSameNumber.get().getId().equals(id)) {
                throw TableNumberException.tableNumberAlreadyExists(diningTableDto.getTableNumber());
            }
        }

        // Kapasite azaltma kontrolü - aktif siparişler varsa kapasite azaltılamaz
        if (diningTableDto.getCapacity() < currentTable.getCapacity()) {
            if (!currentTable.getOrders().isEmpty()) {
                throw TableOrderConflictException.cannotReduceCapacityWithActiveOrders(
                    id, currentTable.getCapacity(), diningTableDto.getCapacity());
            }
            // Gelecek rezervasyonlar varsa kapasite azaltılamaz
            if (!currentTable.getReservations().isEmpty()) {
                throw TableReservationConflictException.cannotReduceCapacityWithFutureReservations(
                    id, currentTable.getCapacity(), diningTableDto.getCapacity());
            }
        }
    }

    public static void validateDeleteDiningTable(Long id, DiningTableRepository diningTableRepository) {
        DiningTable table = diningTableRepository.findById(id)
                .orElseThrow(() -> new TableNotFoundException(id));

        // Durum bazlı silme kontrolü
        String statusName = table.getStatus().getName();
        if ("RESERVED".equals(statusName)) {
            throw TableDeletionException.tableIsReserved(id);
        }
        if ("OCCUPIED".equals(statusName)) {
            throw TableDeletionException.tableIsOccupied(id);
        }

        // Aktif siparişler kontrolü
        if (!table.getOrders().isEmpty()) {
            throw TableDeletionException.tableHasActiveOrders(id, table.getOrders().size());
        }

        // Aktif rezervasyonlar kontrolü
        if (!table.getReservations().isEmpty()) {
            throw TableDeletionException.tableHasActiveReservations(id, table.getReservations().size());
        }
    }

    private static void validateBasicFields(DiningTableRequestDto diningTableDto) {
        // Masa numarası kontrolü
        if (diningTableDto.getTableNumber() == null) {
            throw DiningTableException.validationError("Masa numarası boş olamaz.");
        }
        if (diningTableDto.getTableNumber() <= 0) {
            throw TableNumberException.invalidTableNumber(diningTableDto.getTableNumber());
        }

        // Kapasite kontrolü
        if (diningTableDto.getCapacity() == null) {
            throw DiningTableException.validationError("Masa kapasitesi boş olamaz.");
        }
        if (diningTableDto.getCapacity() <= 0 || diningTableDto.getCapacity() > 20) {
            throw TableCapacityException.invalidCapacity(diningTableDto.getCapacity());
        }

        // Durum kontrolü
        if (diningTableDto.getStatusId() == null) {
            throw DiningTableException.validationError("Masa durumu boş olamaz.");
        }

        // Salon kontrolü
        if (diningTableDto.getSalonId() == null || diningTableDto.getSalonId() <= 0) {
            throw DiningTableException.validationError("Salon ID geçersiz olamaz.");
        }
    }

    /**
     * Masa durumu geçişi için gelişmiş validasyon
     *
     * @param currentStatus Mevcut durum
     * @param newStatus Yeni durum
     */
    public static void validateStatusTransition(TableStatus currentStatus, TableStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw DiningTableException.validationError("Durumlar boş olamaz.");
        }

        String current = currentStatus.getName();
        String target = newStatus.getName();

        // Geçersiz durum geçişleri
        switch (current) {
            case "OCCUPIED":
                if ("AVAILABLE".equals(target)) {
                    throw TableStatusException.invalidStatusTransition(current, target);
                }
                // OCCUPIED -> RESERVED geçişi de yasak (masa boşaltılmadan rezerve edilemez)
                if ("RESERVED".equals(target)) {
                    throw TableStatusException.invalidStatusTransition(current, target);
                }
                break;

            case "RESERVED":
                // RESERVED -> AVAILABLE geçişi kontrolü (rezervasyon iptali gerekebilir)
                if ("AVAILABLE".equals(target)) {
                    throw TableStatusException.invalidStatusTransition(current, target);
                }
                break;

            case "AVAILABLE":
                // AVAILABLE durumundan diğer durumlara geçiş genellikle serbesttir
                break;

            default:
                // Bilinmeyen durumlar
                throw TableStatusException.invalidStatusTransition(current, target);
        }
    }

    /**
     * Masa sipariş için uygunluk kontrolü
     */
    public static void validateTableForOrder(DiningTable table, Integer requiredCapacity) {
        if (table == null) {
            throw new TableNotFoundException("Masa bilgisi bulunamadı");
        }

        // Masa durumu kontrolü
        if (!"AVAILABLE".equals(table.getStatus().getName())) {
            throw TableOrderConflictException.tableNotAvailableForOrder(table.getId(), table.getStatus().getName());
        }

        // Kapasite kontrolü
        if (requiredCapacity != null && requiredCapacity > table.getCapacity()) {
            throw TableOrderConflictException.orderExceedsTableCapacity(
                table.getId(), table.getCapacity(), requiredCapacity);
        }
    }

    /**
     * Masa rezervasyon için uygunluk kontrolü
     */
    public static void validateTableForReservation(DiningTable table, Integer guestCount) {
        if (table == null) {
            throw new TableNotFoundException("Masa bilgisi bulunamadı");
        }

        // Masa durumu kontrolü
        if (!"AVAILABLE".equals(table.getStatus().getName())) {
            throw TableReservationConflictException.tableNotAvailableForReservation(
                table.getId(), table.getStatus().getName());
        }

        // Kapasite kontrolü
        if (guestCount != null && guestCount > table.getCapacity()) {
            throw TableReservationConflictException.reservationExceedsTableCapacity(
                table.getId(), table.getCapacity(), guestCount);
        }
    }
}
