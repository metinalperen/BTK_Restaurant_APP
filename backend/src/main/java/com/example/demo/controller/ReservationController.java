package com.example.demo.controller;

import com.example.demo.dto.request.ReservationRequestDTO;
import com.example.demo.dto.response.ReservationResponseDTO;
import com.example.demo.enums.ReservationStatusConstants;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.DiningTable;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.service.DiningTableService;
import com.example.demo.service.ReservationService;
import com.example.demo.service.UserService;
import com.example.demo.service.AuthService;
import com.example.demo.validation.ReservationValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;
import com.example.demo.dto.response.DiningTableResponseDto;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;

@Tag(
        name = "Reservation Management",
        description = "APIs for managing reservations (CRUD operations, filtering, and status updates)."
)
@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;
    private final DiningTableService diningTableService;
    private final UserService userService;
    private final AuthService authService;
    private final ReservationValidator reservationValidator;
    private final ModelMapper modelMapper;

    public ReservationController(ReservationService reservationService,
                                 DiningTableService diningTableService,
                                 UserService userService,
                                 AuthService authService,
                                 ReservationValidator reservationValidator,
                                 ModelMapper modelMapper) {
        this.reservationService = reservationService;
        this.diningTableService = diningTableService;
        this.userService = userService;
        this.authService = authService;
        this.reservationValidator = reservationValidator;
        this.modelMapper = modelMapper;
    }

    @PostMapping(produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Create a new reservation",
            description = "Creates a new reservation with the provided details."
    )
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Object rawRequest) {

        System.out.println("=== DEBUG: Starting createReservation ===");
        System.out.println("=== DEBUG: Raw request type: " + rawRequest.getClass().getSimpleName());
        System.out.println("=== DEBUG: Raw request: " + rawRequest.toString());
        
        try {
            // JWT token'dan kullanıcı ID'sini çıkar
            Long currentUserId = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                currentUserId = authService.getUserIdFromToken(token);
                System.out.println("=== DEBUG: JWT token'dan çıkarılan kullanıcı ID: " + currentUserId);
            }
            
            if (currentUserId == null) {
                System.err.println("=== ERROR: JWT token'dan kullanıcı ID'si çıkarılamadı ===");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
            }
            
            // Frontend'den gelen veriyi backend formatına dönüştür
            System.out.println("=== DEBUG: Calling transformFrontendData ===");
            ReservationRequestDTO reservationRequestDTO = transformFrontendData(rawRequest, currentUserId);
            System.out.println("=== DEBUG: transformFrontendData completed ===");
            
            // Enhanced debug logging
            System.out.println("=== DEBUG: Received Reservation Request ===");
            System.out.println("Table ID: " + reservationRequestDTO.getTableId());
            System.out.println("Customer Name: " + reservationRequestDTO.getCustomerName());
            System.out.println("Customer Phone: " + reservationRequestDTO.getCustomerPhone());
            System.out.println("Reservation Time: " + reservationRequestDTO.getReservationTime());
            System.out.println("Email: " + reservationRequestDTO.getEmail());
            System.out.println("Person Count: " + reservationRequestDTO.getPersonCount());
            System.out.println("Special Requests: " + reservationRequestDTO.getSpecialRequests());
            System.out.println("Created By: " + reservationRequestDTO.getCreatedBy());
            System.out.println("Status ID: " + reservationRequestDTO.getStatusId());
            
            // Rezervasyon oluştur
            System.out.println("=== DEBUG: Creating reservation... ===");
            Reservation reservation = createReservationFromDTO(reservationRequestDTO);
            System.out.println("=== DEBUG: Reservation created successfully with ID: " + reservation.getId() + " ===");
            
            // Response DTO'ya dönüştür
            ReservationResponseDTO responseDTO = mapToResponseDTO(reservation);
            
            System.out.println("=== DEBUG: createReservation completed successfully ===");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
        } catch (Exception e) {
            System.err.println("=== ERROR in createReservation ===");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping(value = "/test-date-parse", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Test date parsing",
            description = "Test endpoint to validate date parsing functionality."
    )
    public ResponseEntity<Map<String, Object>> testDateParse(@RequestBody Map<String, String> request) {
        try {
            String testDate = request.get("date");
            if (testDate == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "date field is required"));
            }
            
            System.out.println("=== DEBUG: Testing date parse for: " + testDate);
            
            // Test different formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),      // Frontend formatı: 2025-11-16 12:14
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),   // Saniye formatı: 2025-11-16 12:14:00
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),     // Türk formatı: 16/11/2025 12:14
                DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")      // US formatı: 11/16/2025 12:14
            };
            
            LocalDateTime parsedDate = null;
            String usedFormatter = null;
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    parsedDate = LocalDateTime.parse(testDate.trim(), formatter);
                    usedFormatter = formatter.toString();
                    System.out.println("=== DEBUG: Successfully parsed with: " + usedFormatter);
                    break;
                } catch (Exception e) {
                    System.out.println("=== DEBUG: Formatter " + formatter.toString() + " failed: " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("inputDate", testDate);
            response.put("parsedDate", parsedDate != null ? parsedDate.toString() : null);
            response.put("usedFormatter", usedFormatter);
            response.put("success", parsedDate != null);
            
            if (parsedDate != null) {
                response.put("message", "Tarih başarıyla parse edildi!");
                response.put("formattedOutput", parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                response.put("message", "Tarih parse edilemedi!");
                response.put("supportedFormats", List.of(
                    "yyyy-MM-dd HH:mm (örn: 2025-11-16 12:14)",
                    "yyyy-MM-dd HH:mm:ss (örn: 2025-11-16 12:14:00)",
                    "dd/MM/yyyy HH:mm (örn: 16/11/2025 12:14)",
                    "MM/dd/yyyy HH:mm (örn: 11/16/2025 12:14)"
                ));
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Date parse test failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Test başarısız: " + e.getMessage()));
        }
    }

    @GetMapping(produces = "application/json;charset=UTF-8")
    @Operation(summary = "Get all reservations", description = "Retrieves a list of all reservations.")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get reservation by ID",
            description = "Retrieves a specific reservation by its ID."
    )
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @Parameter(description = "ID of the reservation to retrieve", required = true)
            @PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        ReservationResponseDTO responseDTO = mapToResponseDTO(reservation);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(value = "/table/{tableId}", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get reservations by table",
            description = "Retrieves all reservations for a specific dining table."
    )
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByTable(
            @Parameter(description = "ID of the dining table to retrieve reservations for", required = true)
            @PathVariable Long tableId) {
        List<Reservation> reservations = reservationService.getTableReservations(tableId);
        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping(value = "/salon/{salonId}", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get reservations by salon",
            description = "Retrieves all reservations for a specific salon."
    )
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsBySalon(
            @Parameter(description = "ID of the salon to retrieve reservations for", required = true)
            @PathVariable Long salonId) {
        List<Reservation> reservations = reservationService.getSalonReservations(salonId);
        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping(value = "/{id}/cancel", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels a specific reservation by its ID."
    )
    public ResponseEntity<ReservationResponseDTO> cancelReservation(
            @Parameter(description = "ID of the reservation to cancel", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        // JWT token kontrolü
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorizationHeader.substring(7);
        Long currentUserId = authService.getUserIdFromToken(token);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Reservation cancelledReservation = reservationService.cancelReservation(id);
        ReservationResponseDTO responseDTO = mapToResponseDTO(cancelledReservation);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping(value = "/{id}/complete", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Complete a reservation",
            description = "Marks a specific reservation as completed."
    )
    public ResponseEntity<ReservationResponseDTO> completeReservation(
            @Parameter(description = "ID of the reservation to mark as complete", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        // JWT token kontrolü
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorizationHeader.substring(7);
        Long currentUserId = authService.getUserIdFromToken(token);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Reservation completedReservation = reservationService.completeReservation(id);
        ReservationResponseDTO responseDTO = mapToResponseDTO(completedReservation);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping(value = "/{id}/no-show", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Mark reservation as no-show",
            description = "Marks a specific reservation as no-show."
    )
    public ResponseEntity<ReservationResponseDTO> markAsNoShow(
            @Parameter(description = "ID of the reservation to mark as no-show", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        // JWT token kontrolü
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorizationHeader.substring(7);
        Long currentUserId = authService.getUserIdFromToken(token);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Reservation noShowReservation = reservationService.markAsNoShow(id);
        ReservationResponseDTO responseDTO = mapToResponseDTO(noShowReservation);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(value = "/status/{statusId}", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get reservations by status ID",
            description = "Retrieves reservations filtered by their status ID."
    )
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByStatusId(
            @Parameter(description = "Status ID to filter reservations by (1=confirmed, 2=cancelled, 3=completed, 4=no_show, 5=pending)", required = true)
            @PathVariable Integer statusId) {
        List<Reservation> reservations = reservationService.getReservationsByStatus(statusId);
        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping(value = "/today", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get today's reservations",
            description = "Retrieves all reservations for the current day."
    )
    public ResponseEntity<List<ReservationResponseDTO>> getTodayReservations() {
        List<Reservation> reservations = reservationService.getTodayReservations();
        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping(value = "/{id}", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Update a reservation",
            description = "Updates an existing reservation's details."
    )
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @Parameter(description = "ID of the reservation to update", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody ReservationRequestDTO reservationRequestDTO) {

        // JWT token kontrolü
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorizationHeader.substring(7);
        Long currentUserId = authService.getUserIdFromToken(token);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Reservation existingReservation = reservationService.getReservationById(id);

        // Update fields
        existingReservation.setCustomerName(reservationRequestDTO.getFormattedCustomerName());
        existingReservation.setCustomerPhone(reservationRequestDTO.getFormattedPhoneNumber());
        existingReservation.setReservationDate(reservationRequestDTO.getReservationDate());
        existingReservation.setReservationTime(reservationRequestDTO.getReservationTime());
        existingReservation.setSpecialRequests(reservationRequestDTO.getSpecialRequests());
        existingReservation.setEmail(reservationRequestDTO.getEmail());
        existingReservation.setPersonCount(reservationRequestDTO.getPersonCount());

        // Update status if provided
        if (reservationRequestDTO.getStatusId() != null) {
            existingReservation.setStatusId(reservationRequestDTO.getStatusId());
        }

        // Masa tablosundan salon ID'yi al
        DiningTable table = diningTableService.getDiningTableEntityById(Long.valueOf(reservationRequestDTO.getTableId()));
        if (table.getSalon() == null) {
            throw new IllegalArgumentException("Masa bir salona ait değil");
        }
        
        // Update table if changed
        if (!existingReservation.getTable().getId().equals(Long.valueOf(reservationRequestDTO.getTableId()))) {
            DiningTable newTable = diningTableService.getDiningTableEntityById(Long.valueOf(reservationRequestDTO.getTableId()));
            
            // Masa ve salon uyumluluğunu kontrol et
            if (newTable.getSalon() == null) {
                throw new IllegalArgumentException("Seçilen masa belirtilen salona ait değil");
            }
            
            existingReservation.setTable(newTable);
        }

        reservationValidator.validateReservationUpdate(existingReservation);

        Reservation updatedReservation = reservationService.updateReservation(id, existingReservation);
        ReservationResponseDTO responseDTO = mapToResponseDTO(updatedReservation);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping(value = "/date-range", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get reservations by date range",
            description = "Retrieves reservations within a specified date range."
    )
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByDateRange(
            @Parameter(description = "Start date for the range (format: yyyy-MM-dd)", required = true)
            @RequestParam String startDate,
            @Parameter(description = "End date for the range (format: yyyy-MM-dd)", required = true)
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<Reservation> reservations = reservationService.getReservationsByDateRange(start, end);
        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a reservation",
            description = "Deletes a specific reservation by its ID."
    )
    public ResponseEntity<Void> deleteReservation(
            @Parameter(description = "ID of the reservation to delete", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        // JWT token kontrolü
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String token = authorizationHeader.substring(7);
        Long currentUserId = authService.getUserIdFromToken(token);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/test-encoding", produces = "text/plain;charset=UTF-8")
    @Operation(
            summary = "Test character encoding",
            description = "Test endpoint to verify UTF-8 encoding is working properly."
    )
    public ResponseEntity<String> testEncoding() {
        String testMessage = "Türkçe karakterler: ğüşıöçĞÜŞİÖÇ - Test başarılı!";
        return ResponseEntity.ok(testMessage);
    }

    @GetMapping(value = "/test-tables", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Test tables existence",
            description = "Simple endpoint to test if tables exist in database."
    )
    public ResponseEntity<Map<String, Object>> testTables() {
        try {
            System.out.println("=== DEBUG: Testing tables existence ===");
            
            // Get all tables
            List<DiningTableResponseDto> allTables = diningTableService.getAllDiningTables();
            
            // Get available tables
            List<DiningTableResponseDto> availableTables = diningTableService.getAvailableTables();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalTables", allTables.size());
            response.put("availableTables", availableTables.size());
            response.put("allTables", allTables);
            response.put("availableTablesList", availableTables);
            
            System.out.println("=== DEBUG: Total tables: " + allTables.size() + ", Available: " + availableTables.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ERROR: Error testing tables: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Test başarısız: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/available-tables", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get available tables for reservation",
            description = "Retrieves all available tables that can be reserved."
    )
    public ResponseEntity<List<Map<String, Object>>> getAvailableTables() {
        try {
            System.out.println("=== DEBUG: Getting available tables ===");
            List<DiningTableResponseDto> availableTables = diningTableService.getAvailableTables();
            
            List<Map<String, Object>> tableInfo = new ArrayList<>();
            for (DiningTableResponseDto table : availableTables) {
                Map<String, Object> tableData = new HashMap<>();
                tableData.put("id", table.getId());
                tableData.put("tableNumber", table.getTableNumber());
                tableData.put("capacity", table.getCapacity());
                tableData.put("status", table.getStatusName());
                if (table.getSalonId() != null) {
                    tableData.put("salonId", table.getSalonId());
                    tableData.put("salonName", table.getSalonName());
                }
                tableInfo.add(tableData);
            }
            
            System.out.println("=== DEBUG: Found " + tableInfo.size() + " available tables ===");
            return ResponseEntity.ok(tableInfo);
        } catch (Exception e) {
            System.err.println("=== ERROR: Error getting available tables: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Masalar getirilemedi: " + e.getMessage())));
        }
    }

    @GetMapping(value = "/tables", produces = "application/json;charset=UTF-8")
    @Operation(
            summary = "Get all tables",
            description = "Retrieves all tables with their details."
    )
    public ResponseEntity<List<Map<String, Object>>> getAllTables() {
        try {
            System.out.println("=== DEBUG: Getting all tables ===");
            List<DiningTableResponseDto> allTables = diningTableService.getAllDiningTables();
            
            List<Map<String, Object>> tableInfo = new ArrayList<>();
            for (DiningTableResponseDto table : allTables) {
                Map<String, Object> tableData = new HashMap<>();
                tableData.put("id", table.getId());
                tableData.put("tableNumber", table.getTableNumber());
                tableData.put("capacity", table.getCapacity());
                tableData.put("status", table.getStatusName());
                if (table.getSalonId() != null) {
                    tableData.put("salonId", table.getSalonId());
                    tableData.put("salonName", table.getSalonName());
                }
                tableInfo.add(tableData);
            }
            
            System.out.println("=== DEBUG: Found " + tableInfo.size() + " total tables ===");
            return ResponseEntity.ok(tableInfo);
        } catch (Exception e) {
            System.err.println("=== ERROR: Error getting all tables: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Masalar getirilemedi: " + e.getMessage())));
        }
    }

    private Reservation createReservationFromDTO(ReservationRequestDTO dto) {
        // Validation check before processing
        System.out.println("=== DEBUG: Starting validation ===");
        if (dto.getTableId() == null) {
            throw new IllegalArgumentException("Table ID is required");
        }
        System.out.println("=== DEBUG: Validation completed ===");

        // Get dining table - DOUBLE CHECK TABLE EXISTENCE
        System.out.println("=== DEBUG: Getting dining table with ID: " + dto.getTableId());
        DiningTable diningTable = diningTableService.getDiningTableEntityById(Long.valueOf(dto.getTableId()));
        
        if (diningTable == null) {
            throw new IllegalArgumentException("Masa bulunamadı! ID: " + dto.getTableId());
        }
        
        System.out.println("=== DEBUG: Dining table retrieved: " + diningTable.getTableNumber() + " (ID: " + diningTable.getId() + ")");
        
        // Check table status
        if (diningTable.getStatus() != null) {
            System.out.println("=== DEBUG: Table status: " + diningTable.getStatus().getName());
            if ("OCCUPIED".equalsIgnoreCase(diningTable.getStatus().getName()) || 
                "RESERVED".equalsIgnoreCase(diningTable.getStatus().getName())) {
                System.out.println("=== WARNING: Table " + diningTable.getTableNumber() + " is not available for reservation!");
            }
        }
        
        // Check salon connection
        if (diningTable.getSalon() == null) {
            throw new IllegalArgumentException("Masa " + diningTable.getTableNumber() + " bir salona bağlı değil!");
        }
        
        System.out.println("=== DEBUG: Table salon: " + diningTable.getSalon().getName());

        // Create reservation entity
        System.out.println("=== DEBUG: Creating reservation entity ===");
        Reservation reservation = new Reservation();
        reservation.setCustomerName(dto.getCustomerName());
        reservation.setCustomerPhone(dto.getCustomerPhone());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setReservationTime(dto.getReservationTime());
        
        System.out.println("=== DEBUG: Setting reservation date: " + dto.getReservationDate() + " (type: " + (dto.getReservationDate() != null ? dto.getReservationDate().getClass().getSimpleName() : "null") + ")");
        System.out.println("=== DEBUG: Setting reservation time: " + dto.getReservationTime() + " (type: " + (dto.getReservationTime() != null ? dto.getReservationTime().getClass().getSimpleName() : "null") + ")");
        reservation.setSpecialRequests(dto.getSpecialRequests());
        reservation.setEmail(dto.getEmail());
        reservation.setPersonCount(dto.getPersonCount());
        reservation.setStatusId(dto.getStatusId());
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setTable(diningTable);
        
        // Set createdBy user
        System.out.println("=== DEBUG: Setting createdBy user ===");
        User user = userService.getUserById(Long.valueOf(dto.getCreatedBy()))
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
        reservation.setCreatedBy(user);
        System.out.println("=== DEBUG: CreatedBy user set successfully ===");
        
        System.out.println("=== DEBUG: Reservation entity created ===");

        // Validate reservation
        System.out.println("=== DEBUG: Starting reservation validation ===");
        reservationValidator.validateReservationCreation(reservation);
        System.out.println("=== DEBUG: Reservation validation completed ===");

        // Save reservation
        System.out.println("=== DEBUG: Starting to save reservation ===");
        Reservation createdReservation = reservationService.createReservation(reservation);
        System.out.println("=== DEBUG: Reservation saved successfully ===");
        
        return createdReservation;
    }

    private ReservationRequestDTO transformFrontendData(Object rawRequest, Long currentUserId) {
        try {
            System.out.println("=== DEBUG: Raw request received: " + rawRequest.toString());
            
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestMap = objectMapper.convertValue(rawRequest, Map.class);
            
            System.out.println("=== DEBUG: Request map: " + requestMap.toString());
            
            ReservationRequestDTO dto = new ReservationRequestDTO();
            
            // Table ID - Frontend'den gelen tableId alanı aslında masa numarası, bunu gerçek masa ID'sine çevir
            if (requestMap.containsKey("tableId") && requestMap.get("tableId") != null) {
                Object tableNumberObj = requestMap.get("tableId");
                Integer tableNumber;
                
                // tableNumber'ın tipini kontrol et ve dönüştür
                if (tableNumberObj instanceof Integer) {
                    tableNumber = (Integer) tableNumberObj;
                } else if (tableNumberObj instanceof Long) {
                    tableNumber = ((Long) tableNumberObj).intValue();
                } else if (tableNumberObj instanceof String) {
                    tableNumber = Integer.valueOf(tableNumberObj.toString());
                } else {
                    throw new IllegalArgumentException("Geçersiz tableNumber tipi: " + tableNumberObj.getClass().getSimpleName());
                }
                
                System.out.println("=== DEBUG: Frontend'den gelen masa numarası: " + tableNumber);
                
                try {
                    // Masa numarasına göre gerçek masa ID'sini bul
                    System.out.println("=== DEBUG: DiningTableService.findByTableNumber çağrılıyor...");
                    Optional<DiningTable> diningTableOpt = diningTableService.findByTableNumber(tableNumber);
                    
                    if (diningTableOpt.isPresent()) {
                        DiningTable diningTable = diningTableOpt.get();
                        Integer realTableId = diningTable.getId().intValue();
                        dto.setTableId(realTableId);
                        System.out.println("=== DEBUG: Gerçek masa ID'si bulundu: " + realTableId + " (Masa No: " + tableNumber + ")");
                        
                        // Masa durumunu da kontrol et
                        if (diningTable.getStatus() != null) {
                            System.out.println("=== DEBUG: Masa durumu: " + diningTable.getStatus().getName());
                            if ("OCCUPIED".equalsIgnoreCase(diningTable.getStatus().getName()) || 
                                "RESERVED".equalsIgnoreCase(diningTable.getStatus().getName())) {
                                System.out.println("=== WARNING: Masa " + tableNumber + " şu anda müsait değil!");
                            }
                        }
                        
                        // Salon bilgisini de kontrol et
                        if (diningTable.getSalon() != null) {
                            System.out.println("=== DEBUG: Masa salonu: " + diningTable.getSalon().getName() + " (ID: " + diningTable.getSalon().getId() + ")");
                        } else {
                            System.out.println("=== WARNING: Masa " + tableNumber + " bir salona bağlı değil!");
                        }
                        
                    } else {
                        // Mevcut masaları listele ve hata mesajında göster
                        List<DiningTableResponseDto> allTables = diningTableService.getAllDiningTables();
                        StringBuilder availableTables = new StringBuilder();
                        for (DiningTableResponseDto table : allTables) {
                            availableTables.append("Masa ").append(table.getTableNumber())
                                         .append(" (ID: ").append(table.getId()).append("), ");
                        }
                        
                        String errorMsg = "Masa numarası " + tableNumber + " bulunamadı! " +
                                        "Mevcut masalar: " + availableTables.toString().replaceAll(", $", "");
                        System.err.println("=== ERROR: " + errorMsg);
                        throw new IllegalArgumentException(errorMsg);
                    }
                } catch (Exception e) {
                    System.err.println("=== ERROR: Masa bulunurken hata oluştu: " + e.getMessage());
                    e.printStackTrace();
                    throw new IllegalArgumentException("Masa numarası " + tableNumber + " bulunamadı: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("tableId alanı bulunamadı veya null");
            }
            
            // Customer Name - Frontend'den gelen ad + soyad'ı birleştir veya customerName'i kullan
            if (requestMap.containsKey("customerName") && requestMap.get("customerName") != null) {
                dto.setCustomerName(requestMap.get("customerName").toString().trim());
                System.out.println("=== DEBUG: Customer name set: " + dto.getCustomerName());
            } else if (requestMap.containsKey("ad") && requestMap.get("ad") != null &&
                       requestMap.containsKey("soyad") && requestMap.get("soyad") != null) {
                String ad = requestMap.get("ad").toString().trim();
                String soyad = requestMap.get("soyad").toString().trim();
                dto.setCustomerName((ad + " " + soyad).trim());
                System.out.println("=== DEBUG: Customer name set from ad+soyad: " + dto.getCustomerName());
            } else {
                throw new IllegalArgumentException("customerName veya ad+soyad alanları bulunamadı veya null");
            }
            
            // Customer Phone - Frontend'den gelen customerPhone veya telefon'u kullan
            if (requestMap.containsKey("customerPhone") && requestMap.get("customerPhone") != null) {
                dto.setCustomerPhone(requestMap.get("customerPhone").toString());
                System.out.println("=== DEBUG: Customer phone set: " + dto.getCustomerPhone());
            } else if (requestMap.containsKey("telefon") && requestMap.get("telefon") != null) {
                dto.setCustomerPhone(requestMap.get("telefon").toString());
                System.out.println("=== DEBUG: Customer phone set from telefon: " + dto.getCustomerPhone());
            } else {
                throw new IllegalArgumentException("customerPhone veya telefon alanı bulunamadı veya null");
            }
            
            // Reservation Date & Time - Frontend'den gelen tarih ve saati ayrı ayrı işle
            if (requestMap.containsKey("reservationDate") && requestMap.get("reservationDate") != null &&
                requestMap.containsKey("reservationTime") && requestMap.get("reservationTime") != null) {
                
                String reservationDateStr = requestMap.get("reservationDate").toString();
                String reservationTimeStr = requestMap.get("reservationTime").toString();
                
                System.out.println("=== DEBUG: Reservation date received: " + reservationDateStr);
                System.out.println("=== DEBUG: Reservation time received: " + reservationTimeStr);
                
                try {
                    LocalDate date = LocalDate.parse(reservationDateStr);
                    LocalTime time = LocalTime.parse(reservationTimeStr);
                    
                    dto.setReservationDate(date);
                    dto.setReservationTime(time);
                    
                    System.out.println("=== DEBUG: Reservation date and time set successfully: " + date + " " + time);
                    System.out.println("=== DEBUG: DTO reservationDate: " + dto.getReservationDate());
                    System.out.println("=== DEBUG: DTO reservationTime: " + dto.getReservationTime());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Geçersiz tarih veya saat formatı. Tarih: " + reservationDateStr + ", Saat: " + reservationTimeStr);
                }
                
            } else if (requestMap.containsKey("tarih") && requestMap.get("tarih") != null &&
                       requestMap.containsKey("saat") && requestMap.get("saat") != null) {
                
                String tarih = requestMap.get("tarih").toString();
                String saat = requestMap.get("saat").toString();
                
                System.out.println("=== DEBUG: Parsing tarih: " + tarih + ", saat: " + saat);
                
                // Tarih formatını kontrol et
                if (tarih.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    // Saat formatını kontrol et
                    if (saat.matches("\\d{1,2}:\\d{2}")) {
                        try {
                            LocalDate date = LocalDate.parse(tarih);
                            LocalTime time = LocalTime.parse(saat);
                            
                            dto.setReservationDate(date);
                            dto.setReservationTime(time);
                            
                            System.out.println("=== DEBUG: Reservation date and time set successfully: " + date + " " + time);
                            System.out.println("=== DEBUG: DTO reservationDate: " + dto.getReservationDate());
                            System.out.println("=== DEBUG: DTO reservationTime: " + dto.getReservationTime());
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Geçersiz tarih veya saat formatı. Tarih: " + tarih + ", Saat: " + saat);
                        }
                    } else {
                        throw new IllegalArgumentException("Geçersiz saat formatı: " + saat + ". Beklenen format: HH:MM");
                    }
                } else {
                    throw new IllegalArgumentException("Geçersiz tarih formatı: " + tarih + ". Beklenen format: yyyy-MM-dd");
                }
            } else if (requestMap.containsKey("reservationDateTime") && requestMap.get("reservationDateTime") != null) {
                // Frontend'den gelen combined datetime field'ı işle
                String reservationDateTimeStr = requestMap.get("reservationDateTime").toString();
                System.out.println("=== DEBUG: Combined reservationDateTime received: " + reservationDateTimeStr);
                
                try {
                    // ISO format (2025-09-21T12:00) veya diğer formatları dene
                    LocalDateTime dateTime = null;
                    
                    // ISO format deneyin
                    if (reservationDateTimeStr.contains("T")) {
                        try {
                            dateTime = LocalDateTime.parse(reservationDateTimeStr);
                        } catch (Exception e) {
                            // ISO format başarısız, diğer formatları dene
                            System.out.println("=== DEBUG: ISO format failed, trying other formats");
                        }
                    }
                    
                    // Eğer ISO format başarısızsa, diğer formatları dene
                    if (dateTime == null) {
                        DateTimeFormatter[] formatters = {
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
                        };
                        
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                dateTime = LocalDateTime.parse(reservationDateTimeStr, formatter);
                                System.out.println("=== DEBUG: Parsed with formatter: " + formatter.toString());
                                break;
                            } catch (Exception e) {
                                // Bu formatter başarısız, sonrakini dene
                            }
                        }
                    }
                    
                    if (dateTime != null) {
                        dto.setReservationDate(dateTime.toLocalDate());
                        dto.setReservationTime(dateTime.toLocalTime());
                        System.out.println("=== DEBUG: Combined datetime parsed successfully: " + dateTime.toLocalDate() + " " + dateTime.toLocalTime());
                    } else {
                        throw new IllegalArgumentException("Geçersiz datetime formatı: " + reservationDateTimeStr + ". Desteklenen formatlar: ISO (2025-09-21T12:00), yyyy-MM-dd HH:mm, dd/MM/yyyy HH:mm");
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Datetime parse hatası: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("reservationDate+reservationTime, tarih+saat, veya reservationDateTime alanları bulunamadı veya null");
            }
            
            // Special Requests - Frontend'den gelen specialRequest veya not'u kullan
            if (requestMap.containsKey("specialRequest") && requestMap.get("specialRequest") != null) {
                dto.setSpecialRequests(requestMap.get("specialRequest").toString());
                System.out.println("=== DEBUG: Special requests set: " + dto.getSpecialRequests());
            } else if (requestMap.containsKey("not") && requestMap.get("not") != null) {
                dto.setSpecialRequests(requestMap.get("not").toString());
                System.out.println("=== DEBUG: Special requests set from not: " + dto.getSpecialRequests());
            }
            
            // Email (optional)
            if (requestMap.containsKey("email") && requestMap.get("email") != null) {
                dto.setEmail(requestMap.get("email").toString());
                System.out.println("=== DEBUG: Email set: " + dto.getEmail());
            }
            
            // Person Count (optional) - Frontend'den gelen personCount veya kisiSayisi'ni kullan
            if (requestMap.containsKey("personCount") && requestMap.get("personCount") != null) {
                Object personCountObj = requestMap.get("personCount");
                Integer personCount;
                
                if (personCountObj instanceof Integer) {
                    personCount = (Integer) personCountObj;
                } else if (personCountObj instanceof String) {
                    personCount = Integer.valueOf(personCountObj.toString());
                } else {
                    personCount = Integer.valueOf(personCountObj.toString());
                }
                
                dto.setPersonCount(personCount);
                System.out.println("=== DEBUG: Person count set: " + dto.getPersonCount());
            } else if (requestMap.containsKey("kisiSayisi") && requestMap.get("kisiSayisi") != null) {
                Object kisiSayisiObj = requestMap.get("kisiSayisi");
                Integer kisiSayisi;
                
                if (kisiSayisiObj instanceof Integer) {
                    kisiSayisi = (Integer) kisiSayisiObj;
                } else if (kisiSayisiObj instanceof String) {
                    kisiSayisi = Integer.valueOf(kisiSayisiObj.toString());
                } else {
                    kisiSayisi = Integer.valueOf(kisiSayisiObj.toString());
                }
                
                dto.setPersonCount(kisiSayisi);
                System.out.println("=== DEBUG: Person count set from kisiSayisi: " + dto.getPersonCount());
            }
            
            // Status ID - Frontend'den gelen statusId'yi direkt kullan
            if (requestMap.containsKey("statusId") && requestMap.get("statusId") != null) {
                Object statusIdObj = requestMap.get("statusId");
                Integer statusId;
                
                if (statusIdObj instanceof Integer) {
                    statusId = (Integer) statusIdObj;
                } else if (statusIdObj instanceof String) {
                    statusId = Integer.valueOf(statusIdObj.toString());
                } else {
                    statusId = Integer.valueOf(statusIdObj.toString());
                }
                
                dto.setStatusId(statusId);
                System.out.println("=== DEBUG: Status ID set: " + dto.getStatusId());
            } else {
                dto.setStatusId(1); // Default status - confirmed
                System.out.println("=== DEBUG: Default status ID set: " + dto.getStatusId());
            }
            
            // Created By - JWT token'dan gelen kullanıcı ID'sini kullan
            dto.setCreatedBy(currentUserId.intValue());
            System.out.println("=== DEBUG: Created by set from JWT token: " + dto.getCreatedBy());
            
            System.out.println("=== DEBUG: Final DTO: " + dto.toString());
            return dto;
        } catch (Exception e) {
            System.err.println("Frontend veri dönüşüm hatası: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Frontend verisi dönüştürülemedi: " + e.getMessage());
        }
    }

    private ReservationResponseDTO mapToResponseDTO(Reservation reservation) {
        try {
            ReservationResponseDTO responseDTO = new ReservationResponseDTO();
            responseDTO.setId(reservation.getId());
            
            // Salon bilgilerini ekle - null check ekle
            if (reservation.getTable() != null && reservation.getTable().getSalon() != null) {
                responseDTO.setSalonId(reservation.getTable().getSalon().getId());
                responseDTO.setSalonName(reservation.getTable().getSalon().getName());
            }
            
            // Table ID - null check ekle
            if (reservation.getTable() != null) {
                responseDTO.setTableId(reservation.getTable().getId());
            }
            
            responseDTO.setCustomerName(reservation.getCustomerName());
            responseDTO.setCustomerPhone(reservation.getCustomerPhone());
            responseDTO.setReservationDate(reservation.getReservationDate());
            responseDTO.setReservationTime(reservation.getReservationTime());
            responseDTO.setSpecialRequests(reservation.getSpecialRequests());
            responseDTO.setEmail(reservation.getEmail());
            responseDTO.setPersonCount(reservation.getPersonCount());
            responseDTO.setStatusId(reservation.getStatusId());
            responseDTO.setStatusName(reservation.getStatusName());
            responseDTO.setStatusNameInTurkish(reservation.getStatusNameInTurkish());
            
            // CreatedBy null check ekle
            if (reservation.getCreatedBy() != null) {
                responseDTO.setCreatedByName(reservation.getCreatedBy().getName());
            }
            
            responseDTO.setCreatedAt(reservation.getCreatedAt());
            return responseDTO;
        } catch (Exception e) {
            System.err.println("Error mapping reservation to DTO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error mapping reservation data", e);
        }
    }
    
    /**
     * Masa için aktif rezervasyonları tamamla (sipariş alındığında kullanılır)
     */
    @PostMapping("/{tableId}/complete-active")
    public ResponseEntity<List<ReservationResponseDTO>> completeActiveReservationsForTable(@PathVariable Long tableId) {
        try {
            List<Reservation> completedReservations = reservationService.completeActiveReservationsForTable(tableId);
            List<ReservationResponseDTO> responseDTOs = completedReservations.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
