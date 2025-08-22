package com.example.demo.validation;

import java.util.Optional;

import com.example.demo.dto.request.SalonRequestDTO;
import com.example.demo.model.Salon;
import com.example.demo.repository.SalonRepository;

/**
 * Salon validasyon işlemlerini yöneten sınıf
 */
public class SalonValidator {

    /**
     * Yeni salon oluşturma için validasyon
     *
     * @param salonDto Validasyon yapılacak salon DTO'su
     * @param salonRepository Salon repository'si (salon adı kontrolü için)
     */
    public static void validateCreateSalon(SalonRequestDTO salonDto, SalonRepository salonRepository) {
        // Temel validasyonlar
        validateBasicFields(salonDto);

        // Salon adı benzersizlik kontrolü
        Optional<Salon> existingSalon = salonRepository.findByName(salonDto.getName());
        if (existingSalon.isPresent()) {
            throw new IllegalArgumentException("Bu isimde bir salon zaten mevcut: " + salonDto.getName());
        }
    }

    /**
     * Salon güncelleme için validasyon
     *
     * @param id Güncellenecek salon ID'si
     * @param salonDto Validasyon yapılacak salon DTO'su
     * @param salonRepository Salon repository'si
     */
    public static void validateUpdateSalon(Long id, SalonRequestDTO salonDto, SalonRepository salonRepository) {
        // Temel validasyonlar
        validateBasicFields(salonDto);

        // Salon adı benzersizlik kontrolü (kendisi hariç)
        Optional<Salon> existingSalon = salonRepository.findByName(salonDto.getName());
        if (existingSalon.isPresent() && !existingSalon.get().getId().equals(id)) {
            throw new IllegalArgumentException("Bu isimde başka bir salon zaten mevcut: " + salonDto.getName());
        }
    }

    /**
     * Salon silme için validasyon
     *
     * @param salon Silinecek salon entity'si
     */
    public static void validateDeleteSalon(Salon salon) {
        if (salon.getDiningTables() != null && !salon.getDiningTables().isEmpty()) {
            throw new IllegalStateException("Bu salon silinemez çünkü " + salon.getDiningTables().size() + " adet masa bağlı.");
        }
    }

    /**
     * Temel alan validasyonları
     *
     * @param salonDto Validasyon yapılacak salon DTO'su
     */
    private static void validateBasicFields(SalonRequestDTO salonDto) {
        if (salonDto.getName() == null || salonDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Salon adı boş olamaz");
        }

        if (salonDto.getName().length() < 2 || salonDto.getName().length() > 100) {
            throw new IllegalArgumentException("Salon adı 2-100 karakter arasında olmalıdır");
        }

        // capacity artık opsiyonel. Eğer gönderilmişse negatif olamaz.
        if (salonDto.getCapacity() != null && salonDto.getCapacity() < 0) {
            throw new IllegalArgumentException("Salon kapasitesi negatif olamaz");
        }

        if (salonDto.getDescription() != null && salonDto.getDescription().length() > 500) {
            throw new IllegalArgumentException("Salon açıklaması en fazla 500 karakter olabilir");
        }
    }
}
