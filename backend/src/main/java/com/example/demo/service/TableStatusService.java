package com.example.demo.service;

import com.example.demo.exception.diningtable.TableStatusNotFoundException;
import com.example.demo.model.TableStatus;
import com.example.demo.repository.TableStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TableStatus (Masa Durumu) entity'si için servis sınıfı.
 * Masa durumlarını yönetmek için iş mantığını içerir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableStatusService {

    private final TableStatusRepository tableStatusRepository;

    /**
     * Tüm masa durumlarını getirir.
     *
     * @return Tüm TableStatus nesnelerinin listesi
     */
    public List<TableStatus> getAllTableStatuses() {
        log.info("Tüm masa durumları getiriliyor.");
        return tableStatusRepository.findAll();
    }
    @Transactional
    public TableStatus createStatus(TableStatus status) {
        return tableStatusRepository.save(status);
    }

    @Transactional
    public TableStatus updateStatus(Long id, TableStatus status) {
        TableStatus existing = getStatusById(id);
        existing.setName(status.getName());
        return tableStatusRepository.save(existing);
    }

    @Transactional
    public void deleteStatus(Long id) {
        TableStatus existing = getStatusById(id);
        tableStatusRepository.delete(existing);
    }

    /**
     * ID'ye göre masa durumunu getirir.
     *
     * @param id Masa durumu ID'si
     * @return Bulunan TableStatus nesnesi
     * @throws TableStatusNotFoundException Eğer masa durumu bulunamazsa
     */
    public TableStatus getStatusById(Long id) {
        log.info("ID ile masa durumu getiriliyor: id={}", id);
        return tableStatusRepository.findById(id)
                .orElseThrow(() -> new TableStatusNotFoundException("ID ile masa durumu bulunamadı: " + id));
    }

    /**
     * Ada göre bir masa durumunu getirir.
     * Bu metot, DiningTableService gibi diğer servisler tarafından kullanılacaktır.
     *
     * @param name Masa durumunun adı (örneğin: "AVAILABLE")
     * @return Bulunan TableStatus nesnesi
     * @throws TableStatusNotFoundException Eğer masa durumu bulunamazsa
     */
    public TableStatus getStatusByName(String name) {
        log.info("Ad ile masa durumu getiriliyor: name={}", name);
        return tableStatusRepository.findByName(name)
                .orElseThrow(() -> new TableStatusNotFoundException("Ad ile masa durumu bulunamadı: " + name));
    }
}
