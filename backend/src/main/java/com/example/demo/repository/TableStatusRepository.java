
package com.example.demo.repository;

import com.example.demo.model.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Masa durumu (TableStatus) entity'si için repository arayüzü.
 * JpaRepository'den miras alarak CRUD operasyonlarını otomatik olarak sağlar.
 */
@Repository
public interface TableStatusRepository extends JpaRepository<TableStatus, Long> {

    /**
     * Durum adına göre bir masa durumunu bulur.
     * Bu metot, adın benzersiz olduğu varsayılarak kullanılır.
     *
     * @param name Masa durumunun adı (örn: "AVAILABLE")
     * @return Bulunan TableStatus nesnesini içeren Optional
     */
    Optional<TableStatus> findByName(String name);
}
