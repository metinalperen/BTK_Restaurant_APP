package com.example.demo.service;

import com.example.demo.dto.request.StockMovementSummaryRequestDto;
import com.example.demo.dto.response.StockMovementSummaryResponseDto;
import com.example.demo.model.Stock;
import com.example.demo.model.StockMovementSummary;
import com.example.demo.repository.StockMovementSummaryRepository;
import com.example.demo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementSummaryService {

    private final StockMovementSummaryRepository repository;
    private final StockRepository stockRepository;

    // Listeleme
    public List<StockMovementSummaryResponseDto> findAll() {
        return repository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // ID ile getir
    public StockMovementSummaryResponseDto findById(Long id) {
        StockMovementSummary entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("StockMovementSummary not found with id: " + id));
        return convertToResponseDto(entity);
    }

    // Kayıt oluştur
    public StockMovementSummaryResponseDto create(StockMovementSummaryRequestDto dto) {
        StockMovementSummary entity = convertToEntity(dto);
        StockMovementSummary saved = repository.save(entity);
        return convertToResponseDto(saved);
    }

    // Güncelle
    public StockMovementSummaryResponseDto update(Long id, StockMovementSummaryRequestDto dto) {
        StockMovementSummary existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("StockMovementSummary not found with id: " + id));

        // Get the Stock entity for the new stockId
        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + dto.getStockId()));

        existing.setStock(stock);
        existing.setMovementDate(dto.getMovementDate());
        existing.setQuantity(dto.getQuantity());

        StockMovementSummary updated = repository.save(existing);
        return convertToResponseDto(updated);
    }

    // Sil
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // DTO ↔ Entity dönüşüm metodları
    private StockMovementSummaryResponseDto convertToResponseDto(StockMovementSummary entity) {
        StockMovementSummaryResponseDto dto = new StockMovementSummaryResponseDto();
        dto.setId(entity.getId());
        dto.setStockId(entity.getStock().getId());
        dto.setMovementDate(entity.getMovementDate());
        dto.setQuantity(entity.getQuantity());
        return dto;
    }

    private StockMovementSummary convertToEntity(StockMovementSummaryRequestDto dto) {
        StockMovementSummary entity = new StockMovementSummary();

        // Find the Stock entity by ID
        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + dto.getStockId()));

        entity.setStock(stock);
        entity.setMovementDate(dto.getMovementDate());
        entity.setQuantity(dto.getQuantity());
        return entity;
    }

    // Additional service methods using the new repository methods
    public List<StockMovementSummaryResponseDto> findByStockId(Long stockId) {
        return repository.findByStockIdOrderByMovementDateDesc(stockId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<StockMovementSummaryResponseDto> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return repository.findByMovementDateBetweenOrderByMovementDateDesc(startDate, endDate).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<StockMovementSummaryResponseDto> findByMovementDate(LocalDate movementDate) {
        return repository.findByMovementDateOrderByStockId(movementDate).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public Integer getTotalQuantityByStockId(Long stockId) {
        Integer total = repository.getTotalQuantityByStockId(stockId);
        return total != null ? total : 0;
    }

    public Integer getTotalQuantityByDateRange(LocalDate startDate, LocalDate endDate) {
        Integer total = repository.getTotalQuantityByDateRange(startDate, endDate);
        return total != null ? total : 0;
    }
}
