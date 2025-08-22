package com.example.demo.controller;

import com.example.demo.dto.request.StockMovementSummaryRequestDto;
import com.example.demo.dto.response.StockMovementSummaryResponseDto;
import com.example.demo.service.StockMovementSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Stock Movement Summary",
        description = "API for managing stock movement summaries (CRUD operations, retrieval by ID, and listing all summaries)."
)
@RestController
@RequestMapping("/api/stock-movements-summary")
@CrossOrigin(origins = "*")
public class StockMovementSummaryController {

    private final StockMovementSummaryService service;

    public StockMovementSummaryController(StockMovementSummaryService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Get all stock movement summaries",
            description = "Retrieves a list of all stock movement summaries."
    )
    public ResponseEntity<List<StockMovementSummaryResponseDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "Get stock movement summary by ID",
            description = "Retrieves a specific stock movement summary by its ID."
    )
    public ResponseEntity<StockMovementSummaryResponseDto> getById(
            @Parameter(description = "ID of the stock movement summary to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }


    @PostMapping
    @Operation(
            summary = "Create a stock movement summary",
            description = "Creates a new stock movement summary with the provided details."
    )
    public ResponseEntity<StockMovementSummaryResponseDto> create(
            @RequestBody StockMovementSummaryRequestDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a stock movement summary",
            description = "Updates an existing stock movement summary, identified by its ID, with the provided details."
    )
    public ResponseEntity<StockMovementSummaryResponseDto> update(
            @Parameter(description = "ID of the stock movement summary to update", required = true)
            @PathVariable Long id,
            @RequestBody StockMovementSummaryRequestDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }


    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a stock movement summary",
            description = "Deletes a stock movement summary by its ID."
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the stock movement summary to delete", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
