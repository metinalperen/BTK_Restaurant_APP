package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.request.SalonRequestDTO;
import com.example.demo.dto.response.SalonResponseDTO;
import com.example.demo.exception.salon.SalonAlreadyExistsException;
import com.example.demo.exception.salon.SalonDeleteException;
import com.example.demo.exception.salon.SalonNotFoundException;
import com.example.demo.model.DiningTable;
import com.example.demo.model.Salon;
import com.example.demo.repository.SalonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Salon işlemlerini yöneten servis sınıfı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalonService {

    private final SalonRepository salonRepository;
    private final ModelMapper modelMapper;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<SalonResponseDTO> getAllSalons() {
        log.info("Tüm salonlar getiriliyor.");
        List<Salon> salons = salonRepository.findAll();
        return salons.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalonResponseDTO getSalonById(Long id) {
        log.info("ID'ye göre salon getiriliyor: {}", id);
        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new SalonNotFoundException("Salon bulunamadı - ID: " + id));
        return convertToResponseDto(salon);
    }

    @Transactional(readOnly = true)
    public Salon getSalonEntityById(Long id) {
        log.info("ID'ye göre salon entity'si getiriliyor: {}", id);
        return salonRepository.findById(id)
                .orElseThrow(() -> new SalonNotFoundException("Salon bulunamadı - ID: " + id));
    }

    public SalonResponseDTO createSalon(SalonRequestDTO request) {
        log.info("Yeni salon oluşturuluyor: {}", request.getName());

        if (salonRepository.findByName(request.getName()).isPresent()) {
            throw new SalonAlreadyExistsException("Bu isimde bir salon zaten mevcut: " + request.getName());
        }

        Salon salon = convertToEntity(request);
        Salon savedSalon = salonRepository.save(salon);

        activityLogService.logActivity("CREATE", "SALON", savedSalon.getId(),
                "Yeni salon oluşturuldu: " + savedSalon.getName());

        log.info("Salon başarıyla oluşturuldu - ID: {}, Ad: {}", savedSalon.getId(), savedSalon.getName());
        return convertToResponseDto(savedSalon);
    }

    public SalonResponseDTO updateSalon(Long id, SalonRequestDTO request) {
        log.info("Salon güncelleniyor - ID: {}", id);

        Salon existingSalon = salonRepository.findById(id)
                .orElseThrow(() -> new SalonNotFoundException("Güncellenecek salon bulunamadı - ID: " + id));

        salonRepository.findByName(request.getName())
                .filter(salon -> !salon.getId().equals(id))
                .ifPresent(salon -> {
                    throw new SalonAlreadyExistsException("Bu isimde başka bir salon zaten mevcut: " + request.getName());
                });

        existingSalon.setName(request.getName());
        existingSalon.setDescription(request.getDescription());
//      existingSalon.setCapacity(request.getCapacity());
        updateSalonCapacity(existingSalon);

        Salon updatedSalon = salonRepository.save(existingSalon);

        activityLogService.logActivity("UPDATE", "SALON", updatedSalon.getId(),
                "Salon güncellendi: " + updatedSalon.getName());

        log.info("Salon başarıyla güncellendi - ID: {}, Ad: {}", updatedSalon.getId(), updatedSalon.getName());
        return convertToResponseDto(updatedSalon);
    }

    public void deleteSalon(Long id) {
        log.info("Salon siliniyor - ID: {}", id);

        Salon salon = salonRepository.findById(id)
                .orElseThrow(() -> new SalonNotFoundException("Silinecek salon bulunamadı - ID: " + id));

        // Önce dolu masa kontrolü yap
        long occupiedTablesCount = salon.getDiningTables().stream()
                .filter(DiningTable::isOccupied)
                .count();

        if (occupiedTablesCount > 0) {
            throw new SalonDeleteException("Bu salon silinemez çünkü " + occupiedTablesCount + " adet dolu masa bulunuyor. Lütfen önce masaları boşaltın.");
        }

        // Sonra rezerveli masaları kontrol et
        long reservedTablesCount = salon.getDiningTables().stream()
                .filter(table -> table.getStatus() != null && "RESERVED".equalsIgnoreCase(table.getStatus().getName()))
                .count();

        if (reservedTablesCount > 0) {
            throw new SalonDeleteException("Bu salon silinemez çünkü " + reservedTablesCount + " adet rezerveli masa bulunuyor. Lütfen önce rezervasyonları iptal edin.");
        }

        // Eğer herhangi bir masa varsa (boş olsa bile) uyarı ver
        if (!salon.getDiningTables().isEmpty()) {
            throw new SalonDeleteException("Bu salon silinemez çünkü " + salon.getDiningTables().size() + " adet masa bağlı. Lütfen önce masaları silin.");
        }

        salonRepository.deleteById(id);

        activityLogService.logActivity("DELETE", "SALON", id,
                "Salon silindi: " + salon.getName());

        log.info("Salon başarıyla silindi - ID: {}, Ad: {}", id, salon.getName());
    }

    @Transactional(readOnly = true)
    public List<SalonResponseDTO> searchSalonsByName(String name) {
        log.info("Salon adına göre arama yapılıyor: {}", name);
        List<Salon> salons = salonRepository.findByNameContainingIgnoreCase(name);
        return salons.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private SalonResponseDTO convertToResponseDto(Salon salon) {
        SalonResponseDTO dto = new SalonResponseDTO();
        dto.setId(salon.getId());
        dto.setName(salon.getName());
        dto.setDescription(salon.getDescription());
        dto.setCapacity(salon.getCapacity());
        dto.setTotalTables(salon.getTotalTables());

        long occupiedTables = salon.getDiningTables().stream()
                .filter(DiningTable::isOccupied) // DiningTable entity’sinde isOccupied() metodu olmalı
                .count();

        dto.setOccupiedTables((int) occupiedTables);
        dto.setOccupancyRate(salon.getTotalTables() > 0
                ? (occupiedTables * 100.0) / salon.getTotalTables()
                : 0.0);

        return dto;
    }


    private Salon convertToEntity(SalonRequestDTO request) {
        Salon salon = modelMapper.map(request, Salon.class);
        // capacity kullanıcıdan gelmeyebilir; null ise 0'a ayarla (DB'de nullable=false)
        Integer capacity = request.getCapacity();
        salon.setCapacity(capacity != null ? capacity : 0);
        return salon;
    }

    /**
     * Salon bazında ve toplam restoran doluluk oranını hesaplar.
     * @return Map içinde "salons" ve "totalRestaurantOccupancy" bilgileri döner.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOccupancyReport() {
        List<Salon> salons = salonRepository.findAll();

        final int[] totalTables = {0};
        final long[] totalOccupied = {0};

        List<SalonResponseDTO> salonReports = salons.stream().map(salon -> {
            SalonResponseDTO dto = convertToResponseDto(salon);

            int salonTotalTables = salon.getDiningTables().size();
            long salonOccupiedTables = salon.getDiningTables().stream()
                    .filter(table -> table.isOccupied())
                    .count();

            dto.setTotalTables(salonTotalTables);
            dto.setOccupiedTables((int) salonOccupiedTables);
            dto.setOccupancyRate(salonTotalTables > 0
                    ? (salonOccupiedTables * 100.0) / salonTotalTables
                    : 0.0);

            totalTables[0] += salonTotalTables;
            totalOccupied[0] += salonOccupiedTables;

            return dto;
        }).collect(Collectors.toList());

        double totalOccupancyRate = totalTables[0] > 0
                ? (totalOccupied[0] * 100.0) / totalTables[0]
                : 0.0;

        return Map.of(
                "salons", salonReports,
                "totalRestaurantOccupancy", totalOccupancyRate
        );
    }
     public void updateSalonCapacity(Salon salon) {
        int totalCapacity = salon.getDiningTables().stream()
                .mapToInt(table -> table.getCapacity() != null ? table.getCapacity() : 0)
                .sum();
        salon.setCapacity(totalCapacity);
    }
        public Salon saveSalon(Salon salon) {
        return salonRepository.save(salon);
    }

}
