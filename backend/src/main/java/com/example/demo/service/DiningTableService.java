package com.example.demo.service;

import com.example.demo.dto.request.DiningTableRequestDto;
import com.example.demo.dto.response.DiningTableResponseDto;
import com.example.demo.exception.diningtable.DiningTableException;
import com.example.demo.model.DiningTable;
import com.example.demo.model.Salon;
import com.example.demo.model.TableStatus;
import com.example.demo.repository.DiningTableRepository;
import com.example.demo.validation.DiningTableValidator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Masa işlemlerini yöneten servis sınıfı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiningTableService {

    private final DiningTableRepository diningTableRepository;
    private final ModelMapper modelMapper;
    private final SalonService salonService;
    private final ActivityLogService activityLogService;
    private final TableStatusService tableStatusService;

    @Transactional(readOnly = true)
    public List<DiningTableResponseDto> getAllDiningTables() {
        log.info("Tüm masalar getiriliyor.");
        return diningTableRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DiningTableResponseDto getDiningTableById(Long id) {
        log.info("ID ile masa getiriliyor: id={}", id);
        DiningTable diningTable = findTableById(id);
        return convertToResponseDto(diningTable);
    }

    @Transactional(readOnly = true)
    public List<DiningTableResponseDto> getAvailableTables() {
        log.info("Sadece müsait masalar getiriliyor.");
        TableStatus availableStatus = tableStatusService.getStatusByName("AVAILABLE");
        return diningTableRepository.findByStatus(availableStatus)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiningTableResponseDto> getFilteredTables(Integer capacity, String status) {
        log.info("Filtrelenmiş masalar getiriliyor: capacity={}, status={}", capacity, status);
        List<DiningTable> filteredTables;

        if (capacity != null && status != null) {
            TableStatus tableStatus = tableStatusService.getStatusByName(status);
            filteredTables = diningTableRepository.findByCapacityAndStatus(capacity, tableStatus);
        } else if (capacity != null) {
            filteredTables = diningTableRepository.findByCapacity(capacity);
        } else if (status != null) {
            TableStatus tableStatus = tableStatusService.getStatusByName(status);
            filteredTables = diningTableRepository.findByStatus(tableStatus);
        } else {
            filteredTables = diningTableRepository.findAll();
        }

        return filteredTables.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiningTableResponseDto updateDiningTable(Long id, DiningTableRequestDto requestDto) {
        log.info("Masa güncelleniyor: id={}", id);

        // Masayı bul
        DiningTable existingTable = findTableById(id);

        // Validasyon
        DiningTableValidator.validateUpdateDiningTable(id, requestDto, diningTableRepository);

        // Salon değişmişse eski salondan çıkar, yeni salona ekle
        if (!existingTable.getSalon().getId().equals(requestDto.getSalonId())) {
            Salon oldSalon = existingTable.getSalon();
            oldSalon.removeDiningTable(existingTable);

            Salon newSalon = salonService.getSalonEntityById(requestDto.getSalonId());
            newSalon.addDiningTable(existingTable);

            salonService.saveSalon(oldSalon);
            salonService.saveSalon(newSalon);
        }
        // Durum güncellemesi
        TableStatus newStatus = tableStatusService.getStatusById(requestDto.getStatusId());
        // 'RESERVED' durumu için ID=3 zorunluluğu
        if ("RESERVED".equalsIgnoreCase(newStatus.getName()) && (newStatus.getId() == null || newStatus.getId() != 3L)) {
            throw new IllegalArgumentException("'RESERVED' statusunun ID'si 3 olmalıdır (mevcut: " + newStatus.getId() + ")");
        }
        //DiningTableValidator.validateStatusTransition(existingTable.getStatus(), newStatus);

        existingTable.setTableNumber(requestDto.getTableNumber());
        existingTable.setCapacity(requestDto.getCapacity());
        existingTable.setStatus(newStatus);

        // Salonu kaydet (cascade ile DiningTable güncellenir)
        salonService.saveSalon(existingTable.getSalon());

        // Aktivite logu
        ObjectNode details = activityLogService.createDetailsNode(
                "Dining table updated: " + existingTable.getTableNumber(),
                "tableNumber", existingTable.getTableNumber().toString(),
                "capacity", existingTable.getCapacity().toString(),
                "status", existingTable.getStatus().getName(),
                "salonId", existingTable.getSalon().getId().toString()
        );

        activityLogService.logActivity("UPDATE", "DINING_TABLE", existingTable.getId(), details);

        log.info("Masa başarıyla güncellendi: id={}", existingTable.getId());

        return convertToResponseDto(existingTable);
    }




    @Transactional
    public DiningTableResponseDto createDiningTable(DiningTableRequestDto requestDto) {
        log.info("Yeni masa oluşturuluyor: tableNumber={}", requestDto.getTableNumber());

        // Validasyon
        DiningTableValidator.validateCreateDiningTable(requestDto, diningTableRepository);

        // Salon entity'sini al
        Salon salon = salonService.getSalonEntityById(requestDto.getSalonId());

        // Masa entity'sini oluştur
        DiningTable diningTable = new DiningTable();
        diningTable.setTableNumber(requestDto.getTableNumber());
        diningTable.setCapacity(requestDto.getCapacity());

        TableStatus status = (requestDto.getStatusId() != null)
                ? tableStatusService.getStatusById(requestDto.getStatusId())
                : tableStatusService.getStatusByName("AVAILABLE");
        if ("RESERVED".equalsIgnoreCase(status.getName()) && (status.getId() == null || status.getId() != 3L)) {
            throw new IllegalArgumentException("'RESERVED' statusunun ID'si 3 olmalıdır (mevcut: " + status.getId() + ")");
        }

        diningTable.setStatus(status);


        // ❗ Eksik olan kısım: salon set edilmeli
        diningTable.setSalon(salon);

        // 1️⃣ Masa önce DB'ye kaydediliyor (ID oluşması için)
        DiningTable savedTable = diningTableRepository.save(diningTable);

        // 2️⃣ Masa salona ekleniyor ve salon kaydediliyor
        salon.addDiningTable(savedTable);
        salonService.saveSalon(salon);

        // Aktivite logu
        ObjectNode details = activityLogService.createDetailsNode(
                "Dining table created: " + savedTable.getTableNumber(),
                "tableNumber", savedTable.getTableNumber().toString(),
                "capacity", savedTable.getCapacity().toString(),
                "status", savedTable.getStatus().getName(),
                "salonId", salon.getId().toString()
        );

        activityLogService.logActivity("CREATE", "DINING_TABLE", savedTable.getId(), details);

        log.info("Masa başarıyla oluşturuldu: id={}", savedTable.getId());

        // DTO’ya dönüştürüp dön
        return convertToResponseDto(savedTable);
        //return convertToResponseDto(existingTable);
    }


    /**
     * Masa durumunu günceller
     */
    @Transactional
    public void updateTableStatus(Long tableId, String statusName) {
        log.info("Masa durumu güncelleniyor: tableId={}, status={}", tableId, statusName);
        
        DiningTable table = findTableById(tableId);
        TableStatus newStatus;
        if (statusName != null && "RESERVED".equalsIgnoreCase(statusName)) {
            // İŞ KURALI: RESERVED daima ID=3 olmalı
            newStatus = tableStatusService.getStatusById(3L);
        } else {
            newStatus = tableStatusService.getStatusByName(statusName);
        }
        
        if (newStatus == null) {
            throw new IllegalArgumentException("Geçersiz durum adı: " + statusName);
        }
        
        table.setStatus(newStatus);
        diningTableRepository.save(table);
        
        log.info("Masa durumu başarıyla güncellendi: tableId={}, newStatus={}", tableId, statusName);
    }

    @Transactional
    public void deleteDiningTable(Long id) {
        log.info("Masa siliniyor: id={}", id);

        DiningTableValidator.validateDeleteDiningTable(id, diningTableRepository);

        DiningTable tableToDelete = findTableById(id);

        ObjectNode details = activityLogService.createDetailsNode(
                "Dining table deleted: " + tableToDelete.getTableNumber(),
                "tableNumber", tableToDelete.getTableNumber().toString(),
                "capacity", tableToDelete.getCapacity().toString(),
                "status", tableToDelete.getStatus().getName()
        );

        activityLogService.logActivity("DELETE", "DINING_TABLE", tableToDelete.getId(), details);

        Salon salon = tableToDelete.getSalon();

        // Masayı salondan çıkar (updateStats ile totalTables ve capacity güncellenir)
        salon.removeDiningTable(tableToDelete);

        // Masayı sil
        diningTableRepository.delete(tableToDelete);

        // Salonu kaydet
        salonService.saveSalon(salon);

        log.info("Masa başarıyla silindi: id={}", id);
    }




    // ==================== PRIVATE HELPER METHODS ====================

    private DiningTable findTableById(Long id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> DiningTableException.tableNotFound(id));
    }

    private DiningTableResponseDto convertToResponseDto(DiningTable diningTable) {
        DiningTableResponseDto dto = modelMapper.map(diningTable, DiningTableResponseDto.class);
        if (diningTable.getSalon() != null) {
            dto.setSalonId(diningTable.getSalon().getId());
            dto.setSalonName(diningTable.getSalon().getName());
        }
        if (diningTable.getStatus() != null) {
            dto.setStatusId(diningTable.getStatus().getId());
            dto.setStatusName(diningTable.getStatus().getName());
        }
        
        // Order durumunu kontrol et
        dto.setHasActiveOrder(diningTable.hasActiveOrder());
        dto.setHasCompletedOrder(diningTable.hasCompletedOrder());
        
        // Aktif order items sayısını set et
        int activeItemsCount = diningTable.getActiveOrderItemsCount();
        dto.setActiveOrderItemsCount(activeItemsCount);
        
        // Gerçek durumu hesapla: order durumuna göre
        String realStatus;
        if (diningTable.hasActiveOrder() && activeItemsCount > 0) {
            realStatus = "OCCUPIED"; // Kırmızı - aktif order var ve items var
        } else if (diningTable.hasCompletedOrder()) {
            realStatus = "AVAILABLE"; // Yeşil - tamamlanmış order var
        } else {
            // Order yoksa veya boş order varsa mevcut status'ü kullan
            realStatus = diningTable.getStatus() != null ? diningTable.getStatus().getName() : "AVAILABLE";
        }
        dto.setRealStatus(realStatus);
        
        return dto;
    }

    @Transactional(readOnly = true)
    public DiningTable getDiningTableEntityById(Long id) {
        return diningTableRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Masa bulunamadı: " + id));
    }

    @Transactional(readOnly = true)
    public List<DiningTable> getAllTables() {
        log.info("Tüm masalar (entity) getiriliyor.");
        return diningTableRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DiningTableResponseDto> getTablesBySalon(Long salonId) {
        log.info("Salon ID'ye göre masalar getiriliyor: salonId={}", salonId);
        return diningTableRepository.findBySalonId(salonId)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiningTableResponseDto> getAvailableTablesBySalon(Long salonId) {
        log.info("Salon ID'ye göre müsait masalar getiriliyor: salonId={}", salonId);
        TableStatus availableStatus = tableStatusService.getStatusByName("AVAILABLE");
        return diningTableRepository.findBySalonIdAndStatus(salonId, availableStatus)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DiningTable> findByTableNumber(Integer tableNumber) {
        log.info("Masa numarasına göre masa aranıyor: tableNumber={}", tableNumber);
        return diningTableRepository.findByTableNumber(tableNumber);
    }
}