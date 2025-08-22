package com.example.demo.repository;

import com.example.demo.model.DiningTable;
import com.example.demo.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DiningTable (masa) veritabanı işlemlerini yöneten repository arayüzü.
 */
@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    /**
     * Verilen masa numarasına (tableNumber) sahip bir masayı bulur.
     * Bu metot, masa numarası benzersizlik kontrolü için kullanılır.
     *
     * @param tableNumber aranacak masa numarası
     * @return Eğer masa bulunursa Optional içinde DiningTable nesnesi, bulunamazsa boş Optional
     */
    Optional<DiningTable> findByTableNumber(Integer tableNumber);

    /**
     * Belirli bir duruma (status) sahip tüm masaları getirir.
     * Örneğin, müsait (AVAILABLE) masaları listelemek için kullanılabilir.
     *
     * @param status aranacak masa durumu
     * @return Belirtilen durumdaki masaların listesi
     */
    List<DiningTable> findByStatus(TableStatus status);

    // YENİ: Belirli bir kapasiteye sahip masaları bulur
    List<DiningTable> findByCapacity(Integer capacity);

    // YENİ: Belirli bir kapasiteye ve duruma sahip masaları bulur
    List<DiningTable> findByCapacityAndStatus(Integer capacity, TableStatus status);

    /**
     * Belirli bir salona ait tüm masaları getirir.
     *
     * @param salonId Salon ID'si
     * @return Belirtilen salona ait masaların listesi
     */
    List<DiningTable> findBySalonId(Long salonId);

    /**
     * Belirli bir salona ait ve belirli bir durumda olan masaları getirir.
     *
     * @param salonId Salon ID'si
     * @param status Masa durumu
     * @return Belirtilen salona ait ve belirtilen durumdaki masaların listesi
     */
    List<DiningTable> findBySalonIdAndStatus(Long salonId, TableStatus status);
}
