package com.example.demo.repository;

import com.example.demo.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Salon veritabanı işlemlerini yöneten repository arayüzü.
 */
@Repository
public interface SalonRepository extends JpaRepository<Salon, Long> {

    /**
     * Verilen salon adına sahip bir salonu bulur.
     *
     * @param name aranacak salon adı
     * @return Eğer salon bulunursa Optional içinde Salon nesnesi, bulunamazsa boş Optional
     */
    Optional<Salon> findByName(String name);

    /**
     * Salon adına göre case-insensitive arama yapar.
     *
     * @param name aranacak salon adı
     * @return Belirtilen ada sahip salonların listesi
     */
    List<Salon> findByNameContainingIgnoreCase(String name);
}
