package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.DiningTableRequestDto;
import com.example.demo.dto.response.DiningTableResponseDto;
import com.example.demo.service.DiningTableService;
import com.example.demo.service.TableStatusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Tag(name = "Dining Table Management", description = "APIs for managing dining tables (CRUD operations, filtering, and status updates).")
@Slf4j
@RestController
@RequestMapping("/api/dining-tables")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DiningTableController {

    private final DiningTableService diningTableService;
    private final TableStatusService tableStatusService;

    // Yardımcı metod: status String'ini statusId Long'a çevirir
    private Long convertStatusToId(String status) {
        if (status == null) throw new IllegalArgumentException("Status cannot be null");
        // RESERVED daima 3 olmalı; diğerleri dinamik olabilir
        if ("RESERVED".equalsIgnoreCase(status)) {
            return 3L;
        }
        return tableStatusService.getStatusByName(status.toUpperCase()).getId();
    }

    @GetMapping
    @Operation(
        summary = "Get all dining tables",
        description = "Retrieves a list of all dining tables in the restaurant with their current status and details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all dining tables",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DiningTableResponseDto>> getAllDiningTables() {
        log.info("Tüm masalar getiriliyor");
        return ResponseEntity.ok(diningTableService.getAllDiningTables());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get dining table by ID",
        description = "Retrieves a specific dining table by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dining table",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Dining table not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DiningTableResponseDto> getDiningTableById(
        @Parameter(description = "Unique identifier of the dining table", example = "1", required = true)
        @PathVariable Long id) {
        log.info("ID ile masa getiriliyor: id={}", id);
        return ResponseEntity.ok(diningTableService.getDiningTableById(id));
    }

    @GetMapping("/available")
    @Operation(
        summary = "Get available dining tables",
        description = "Retrieves a list of all dining tables that are currently available for seating."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available dining tables",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DiningTableResponseDto>> getAvailableTables() {
        log.info("Müsait masalar getiriliyor");
        return ResponseEntity.ok(diningTableService.getAvailableTables());
    }

    @GetMapping("/salon/{salonId}")
    @Operation(
        summary = "Get dining tables by salon",
        description = "Retrieves all dining tables that belong to a specific salon."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved dining tables for salon",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Salon not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DiningTableResponseDto>> getTablesBySalon(
        @Parameter(description = "Unique identifier of the salon", example = "1", required = true)
        @PathVariable Long salonId) {
        log.info("Salon ID'ye göre masalar getiriliyor: salonId={}", salonId);
        return ResponseEntity.ok(diningTableService.getTablesBySalon(salonId));
    }

    @GetMapping("/salon/{salonId}/available")
    @Operation(
        summary = "Get available dining tables by salon",
        description = "Retrieves all available dining tables that belong to a specific salon."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available dining tables for salon",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Salon not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DiningTableResponseDto>> getAvailableTablesBySalon(
        @Parameter(description = "Unique identifier of the salon", example = "1", required = true)
        @PathVariable Long salonId) {
        log.info("Salon ID'ye göre müsait masalar getiriliyor: salonId={}", salonId);
        return ResponseEntity.ok(diningTableService.getAvailableTablesBySalon(salonId));
    }

    @PostMapping
    @Operation(
        summary = "Create a new dining table",
        description = "Creates a new dining table with the specified details including table number, capacity, status, and salon assignment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dining table created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"id\": 1, \"tableNumber\": 5, \"capacity\": 6, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Table number already exists in the salon"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DiningTableResponseDto> createDiningTable(
        @Parameter(description = "Dining table creation request", required = true)
        @Valid @RequestBody DiningTableRequestDto requestDto) {
        log.info("Yeni masa oluşturuluyor: tableNumber={}", requestDto.getTableNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(diningTableService.createDiningTable(requestDto));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update dining table",
        description = "Updates an existing dining table with new details. All fields must be provided."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dining table updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"id\": 1, \"tableNumber\": 5, \"capacity\": 8, \"statusId\": 2, \"statusName\": \"OCCUPIED\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Dining table not found"),
        @ApiResponse(responseCode = "409", description = "Table number already exists in the salon"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DiningTableResponseDto> updateDiningTable(
        @Parameter(description = "Unique identifier of the dining table", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "Updated dining table data", required = true)
        @Valid @RequestBody DiningTableRequestDto requestDto) {
        log.info("Masa güncelleniyor: id={}", id);
        return ResponseEntity.ok(diningTableService.updateDiningTable(id, requestDto));
    }
    @PatchMapping("/{id}/status/{status}")
    @Operation(
        summary = "Update dining table status",
        description = "Updates only the status of a dining table. Available statuses: AVAILABLE, OCCUPIED, RESERVED, CLEANING, MAINTENANCE"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table status updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 2, \"statusName\": \"OCCUPIED\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Dining table not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DiningTableResponseDto> updateTableStatus(
        @Parameter(description = "Unique identifier of the dining table", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "New status for the table", example = "OCCUPIED", required = true)
        @PathVariable String status) {
        log.info("Masa durumu güncelleniyor: id={}, status={}", id, status);
        DiningTableResponseDto currentTable = diningTableService.getDiningTableById(id);
        DiningTableRequestDto updateDto = new DiningTableRequestDto();
        updateDto.setTableNumber(currentTable.getTableNumber());
        updateDto.setCapacity(currentTable.getCapacity());
        updateDto.setStatusId(convertStatusToId(status));
        updateDto.setSalonId(currentTable.getSalonId());
        return ResponseEntity.ok(diningTableService.updateDiningTable(id, updateDto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete dining table",
        description = "Permanently deletes a dining table from the system."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dining table deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Dining table not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteDiningTable(
        @Parameter(description = "Unique identifier of the dining table to delete", example = "1", required = true)
        @PathVariable Long id) {
        log.info("Masa siliniyor: id={}", id);
        diningTableService.deleteDiningTable(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/capacity/{capacity}")
    @Operation(
        summary = "Update dining table capacity",
        description = "Updates only the capacity of a dining table."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table capacity updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"id\": 1, \"tableNumber\": 1, \"capacity\": 6, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Dining table not found"),
        @ApiResponse(responseCode = "400", description = "Invalid capacity value"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DiningTableResponseDto> updateTableCapacity(
        @Parameter(description = "Unique identifier of the dining table", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "New capacity for the table", example = "6", required = true)
        @PathVariable Integer capacity) {
        log.info("Masa kapasitesi güncelleniyor: id={}, capacity={}", id, capacity);
        DiningTableResponseDto currentTable = diningTableService.getDiningTableById(id);
        DiningTableRequestDto updateDto = new DiningTableRequestDto();
        updateDto.setTableNumber(currentTable.getTableNumber());
        updateDto.setCapacity(capacity);
        updateDto.setStatusId(currentTable.getStatusId());
        updateDto.setSalonId(currentTable.getSalonId());
        return ResponseEntity.ok(diningTableService.updateDiningTable(id, updateDto));
    }

    @PatchMapping("/{id}/table-number/{tableNumber}")
    @Operation(
        summary = "Update dining table number",
        description = "Updates only the table number of a dining table."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Table number updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"id\": 1, \"tableNumber\": 5, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Dining table not found"),
        @ApiResponse(responseCode = "409", description = "Table number already exists in the salon"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DiningTableResponseDto> updateTableNumber(
        @Parameter(description = "Unique identifier of the dining table", example = "1", required = true)
        @PathVariable Long id,
        @Parameter(description = "New table number", example = "5", required = true)
        @PathVariable Integer tableNumber) {
        log.info("Masa numarası güncelleniyor: id={}, tableNumber={}", id, tableNumber);
        DiningTableResponseDto currentTable = diningTableService.getDiningTableById(id);
        DiningTableRequestDto updateDto = new DiningTableRequestDto();
        updateDto.setTableNumber(tableNumber);
        updateDto.setCapacity(currentTable.getCapacity());
        updateDto.setStatusId(currentTable.getStatusId());
        updateDto.setSalonId(currentTable.getSalonId());
        return ResponseEntity.ok(diningTableService.updateDiningTable(id, updateDto));
    }

    @GetMapping("/filter")
    @Operation(
        summary = "Filter dining tables",
        description = "Retrieves dining tables filtered by capacity and/or status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered dining tables",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DiningTableResponseDto.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"id\": 1, \"tableNumber\": 1, \"capacity\": 4, \"statusId\": 1, \"statusName\": \"AVAILABLE\", \"salonId\": 1, \"salonName\": \"Ana Salon\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DiningTableResponseDto>> getFilteredTables(
        @Parameter(description = "Filter by table capacity", example = "4", required = false)
        @RequestParam(required = false) Integer capacity,
        @Parameter(description = "Filter by table status", example = "AVAILABLE", required = false)
        @RequestParam(required = false) String status) {
        log.info("Filtrelenmiş masalar getiriliyor: capacity={}, status={}", capacity, status);
        return ResponseEntity.ok(diningTableService.getFilteredTables(capacity, status));
    }
}