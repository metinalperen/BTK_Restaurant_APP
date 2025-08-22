package com.example.demo.controller;

import com.example.demo.model.TableStatus;
import com.example.demo.service.TableStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/table-statuses")
@RequiredArgsConstructor
public class TableStatusController {

    private final TableStatusService tableStatusService;

    /**
     * Tüm masa durumlarını getirir
     */
    @GetMapping
    public ResponseEntity<List<TableStatus>> getAllTableStatuses() {
        log.info("GET /api/table-statuses - Tüm masa durumları isteniyor.");
        return ResponseEntity.ok(tableStatusService.getAllTableStatuses());
    }

    /**
     * ID'ye göre masa durumu getirir
     */
    @GetMapping("/{id}")
    public ResponseEntity<TableStatus> getStatusById(@PathVariable Long id) {
        log.info("GET /api/table-statuses/{} - ID ile masa durumu isteniyor.", id);
        return ResponseEntity.ok(tableStatusService.getStatusById(id));
    }

    /**
     * İsme göre masa durumu getirir
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<TableStatus> getStatusByName(@PathVariable String name) {
        log.info("GET /api/table-statuses/by-name/{} - İsme göre masa durumu isteniyor.", name);
        return ResponseEntity.ok(tableStatusService.getStatusByName(name));
    }

    /**
     * Yeni masa durumu ekler
     */
    @PostMapping
    public ResponseEntity<TableStatus> createStatus(@RequestBody TableStatus status) {
        log.info("POST /api/table-statuses - Yeni masa durumu ekleniyor: {}", status.getName());
        TableStatus createdStatus = tableStatusService.createStatus(status);
        return ResponseEntity.ok(createdStatus);
    }

    /**
     * Masa durumunu günceller
     */
    @PutMapping("/{id}")
    public ResponseEntity<TableStatus> updateStatus(@PathVariable Long id, @RequestBody TableStatus status) {
        log.info("PUT /api/table-statuses/{} - Masa durumu güncelleniyor.", id);
        TableStatus updatedStatus = tableStatusService.updateStatus(id, status);
        return ResponseEntity.ok(updatedStatus);
    }

    /**
     * Masa durumunu siler
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable Long id) {
        log.info("DELETE /api/table-statuses/{} - Masa durumu siliniyor.", id);
        tableStatusService.deleteStatus(id);
        return ResponseEntity.noContent().build();
    }
}
