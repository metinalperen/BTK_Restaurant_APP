package com.example.demo.controller;

import com.example.demo.dto.request.StockRequestDTO;
import com.example.demo.dto.response.StockResponseDTO;
import com.example.demo.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(
        name = "Stock Management",
        description = "API for managing stocks (CRUD operations)."
)
@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    public final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    @Operation(
            summary = "Get all stocks",
            description = "Retrieves a list of all stocks."
    )
    public ResponseEntity<List<StockResponseDTO>> getAllStocks() {
        List<StockResponseDTO> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get stock by ID",
            description = "Retrieves a specific stock by its ID."
    )
    public ResponseEntity<StockResponseDTO> getStockById(
            @Parameter(description = "ID of the stock to retrieve", required = true)
            @PathVariable Long id) {
        StockResponseDTO stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    @PostMapping
    @Operation(
            summary = "Create a new stock",
            description = "Creates a new stock with the provided details."
    )
    public ResponseEntity<StockResponseDTO> createStock(@RequestBody StockRequestDTO requestDTO) {
        StockResponseDTO created = stockService.createStock(requestDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an stock",
            description = "Updates an existing stock's details."
    )
    public ResponseEntity<StockResponseDTO> updateStock(
            @Parameter(description = "ID of the stock to update", required = true)
            @PathVariable Long id,
            @RequestBody StockRequestDTO requestDTO
        ) {
        StockResponseDTO updated = stockService.updateStock(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an stock",
            description = "Deletes an stock by its ID."
    )
    public ResponseEntity<Void> deleteStock(
            @Parameter(description = "ID of the stock to delete", required = true)
            @PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/min-quantity")
    @Operation(
            summary = "Update minimum stock quantity",
            description = "Updates only the minimum required stock quantity of a stock."
    )
    public ResponseEntity<StockResponseDTO> updateMinQuantity(
            @Parameter(description = "ID of the stock to update", required = true)
            @PathVariable Long id,
            @RequestParam BigDecimal minQuantity
    ) {
        StockResponseDTO updated = stockService.updateMinQuantity(id, minQuantity);
        return ResponseEntity.ok(updated);
    }


}
