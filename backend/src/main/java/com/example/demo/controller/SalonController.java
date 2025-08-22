package com.example.demo.controller;

import com.example.demo.dto.request.SalonRequestDTO;
import com.example.demo.dto.response.SalonResponseDTO;
import com.example.demo.service.SalonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Salon Management",
        description = "APIs for managing salons (CRUD operations, filtering, and searching)."
)
@Slf4j
@RestController
@RequestMapping("/api/salons")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SalonController {

    private final SalonService salonService;

    @GetMapping
    @Operation(
            summary = "Get all salons",
            description = "Retrieves a list of all salons."
    )
    public ResponseEntity<List<SalonResponseDTO>> getAllSalons() {
        log.info("Tüm salonlar getiriliyor");
        List<SalonResponseDTO> salons = salonService.getAllSalons();
        return ResponseEntity.ok(salons);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get salon by ID",
            description = "Retrieves a specific salon by its ID."
    )
    public ResponseEntity<SalonResponseDTO> getSalonById(
            @Parameter(description = "Salon ID", required = true)
            @PathVariable Long id) {
        log.info("ID'ye göre salon getiriliyor: {}", id);
        SalonResponseDTO salon = salonService.getSalonById(id);
        return ResponseEntity.ok(salon);
    }

    @PostMapping
    @Operation(
            summary = "Create new salon",
            description = "Creates a new salon with the provided information."
    )
    public ResponseEntity<SalonResponseDTO> createSalon(
            @Parameter(description = "Salon creation request", required = true)
            @Valid @RequestBody SalonRequestDTO request) {
        log.info("Yeni salon oluşturuluyor: {}", request.getName());
        SalonResponseDTO createdSalon = salonService.createSalon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSalon);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update salon",
            description = "Updates an existing salon with the provided information."
    )
    public ResponseEntity<SalonResponseDTO> updateSalon(
            @Parameter(description = "Salon ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Salon update request", required = true)
            @Valid @RequestBody SalonRequestDTO request) {
        log.info("Salon güncelleniyor - ID: {}", id);
        SalonResponseDTO updatedSalon = salonService.updateSalon(id, request);
        return ResponseEntity.ok(updatedSalon);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete salon",
            description = "Deletes a salon by its ID."
    )
    public ResponseEntity<Void> deleteSalon(
            @Parameter(description = "Salon ID", required = true)
            @PathVariable Long id) {
        log.info("Salon siliniyor - ID: {}", id);
        salonService.deleteSalon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search salons by name",
            description = "Searches for salons containing the specified name (case-insensitive)."
    )
    public ResponseEntity<List<SalonResponseDTO>> searchSalonsByName(
            @Parameter(description = "Salon name to search for", required = true)
            @RequestParam String name) {
        log.info("Salon adına göre arama: {}", name);
        List<SalonResponseDTO> salons = salonService.searchSalonsByName(name);
        return ResponseEntity.ok(salons);
    }
    @GetMapping("/occupancy")
    @Operation(
            summary = "Get salon occupancy report",
            description = "Returns occupancy rate for each salon and total restaurant occupancy."
    )
    public ResponseEntity<Map<String, Object>> getOccupancyReport() {
        log.info("Salon doluluk raporu getiriliyor");
        Map<String, Object> occupancyReport = salonService.getOccupancyReport();
        return ResponseEntity.ok(occupancyReport);
    }


}


