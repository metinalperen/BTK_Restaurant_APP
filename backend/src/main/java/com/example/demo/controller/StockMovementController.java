package com.example.demo.controller;

import com.example.demo.service.StockMovementService;
import com.example.demo.validation.StockMovementValidator;

import com.example.demo.dto.request.StockMovementRequestDTO;
import com.example.demo.dto.response.StockMovementResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Stock Movement",
        description = "API for managing stock movements (CRUD operations, retrieval by ID, stock ID, reason, date range, and total stock change)."
)
@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final StockMovementValidator stockMovementValidator;


    @PostMapping
    @Operation(
            summary = "Create a stock movement",
            description = "Records a new stock movement for a stock."
    )
    public StockMovementResponseDTO createStockMovement(
            @RequestBody StockMovementRequestDTO requestDTO) {
        stockMovementValidator.validateStockMovement(requestDTO);
        return stockMovementService.createStockMovement(requestDTO);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Get stock movement by ID",
            description = "Retrieves a specific stock movement by its ID."
    )
    public StockMovementResponseDTO getById(
            @Parameter(description = "ID of the stock movement to retrieve", required = true)
            @PathVariable Long id) {
        return stockMovementService.getStockMovementById(id);
    }


    @GetMapping
    @Operation(
            summary = "Get all stock movements",
            description = "Retrieves a list of all stock movements."
    )
    public List<StockMovementResponseDTO> getAll() {
        return stockMovementService.getAllStockMovements();
    }


    @GetMapping("/stock/{stockId}")
    @Operation(
            summary = "Get stock movements by stock ID",
            description = "Retrieves all stock movements for a specific stock."
    )
    public List<StockMovementResponseDTO> getByStockId(
            @Parameter(description = "ID of the stock to retrieve stock movements for", required = true)
            @PathVariable Long stockId) {
        return stockMovementService.getStockMovementsByStockId(stockId);
    }


    @GetMapping("/reason/{reason}")
    @Operation(
            summary = "Get stock movements by reason",
            description = "Retrieves stock movements filtered by a specific reason."
    )
    public List<StockMovementResponseDTO> getByReason(
            @Parameter(description = "Reason to filter stock movements by", required = true)
            @PathVariable String reason) {
        return stockMovementService.getStockMovementsByReason(
                com.example.demo.enums.StockMovementEnum.valueOf(reason)
        );
    }


    @GetMapping("/date-range")
    @Operation(
            summary = "Get stock movements by date range",
            description = "Retrieves stock movements within a specified date range."
    )
    public List<StockMovementResponseDTO> getByDateRange(
            @Parameter(description = "Start date and time for the range", required = true)
            @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date and time for the range", required = true)
            @RequestParam LocalDateTime endDate) {
        return stockMovementService.getStockMovementsByDateRange(startDate, endDate);
    }


    @GetMapping("/total-change/{stockId}")
    @Operation(
            summary = "Get total stock change for a stock",
            description = "Calculates the total stock change for a specific stock."
    )
    public Integer getTotalStockChange(
            @Parameter(description = "ID of the stock to calculate stock change for", required = true)
            @PathVariable Long stockId) {
        return stockMovementService.getTotalStockChangeByStockId(stockId);
    }

    // Yeni eklenen endpointler:
    @PutMapping("/{id}")
    @Operation(summary = "Update a stock movement", description = "Updates an existing stock movement.")
    public StockMovementResponseDTO updateStockMovement(
            @Parameter(description = "ID of the stock movement to update", required = true)
            @PathVariable Long id,
            @RequestBody StockMovementRequestDTO requestDTO) {
        stockMovementValidator.validateStockMovement(requestDTO);
        return stockMovementService.updateStockMovement(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stock movement", description = "Deletes a stock movement by its ID.")
    public void deleteStockMovement(
            @Parameter(description = "ID of the stock movement to delete", required = true)
            @PathVariable Long id) {
        stockMovementService.deleteStockMovement(id);
    }
}