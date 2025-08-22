package com.example.demo.service;
import com.example.demo.enums.StockUnit;

import com.example.demo.dto.request.StockRequestDTO;
import com.example.demo.dto.response.StockResponseDTO;
import com.example.demo.exception.stock.StockConflictException;
import com.example.demo.exception.stock.StockNotFoundException;
import com.example.demo.model.Stock;
import com.example.demo.repository.StockRepository;
import com.example.demo.utils.BDH;
import com.example.demo.validation.StockValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockValidator stockValidator;
    private final ActivityLogService activityLogService;

    public StockService(StockRepository stockRepository, StockValidator stockValidator, ActivityLogService activityLogService) {
        this.stockRepository = stockRepository;
        this.stockValidator = stockValidator;
        this.activityLogService = activityLogService;
    }

    // Entity -> Response DTO
    private StockResponseDTO convertToResponseDTO(Stock stock) {
        StockResponseDTO dto = new StockResponseDTO();
        dto.setId(stock.getId());
        dto.setName(stock.getName());
        dto.setUnit(stock.getUnit()); // enum olarak döner
        dto.setMinQuantity(stock.getMinQuantity());
        dto.setStockQuantity(stock.getQuantity());
        return dto;
    }

    // Request DTO -> Entity
    private Stock convertToEntity(StockRequestDTO dto) {
        Stock stock = new Stock();
        stock.setName(dto.getName());
        stock.setUnit(dto.getUnit()); // enum doğrudan set edilir
        stock.setQuantity(dto.getStockQuantity());
        stock.setMinQuantity(dto.getMinQuantity());

        return stock;
    }

    private String normalizedName(String name) {
        return name == null ? null : name.trim().replaceAll("\\s+", " ");
    }

    // Tüm malzemeleri getir
    public List<StockResponseDTO> getAllStocks() {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ID ile malzeme getir
    public StockResponseDTO getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock not found, ID: " + id));
        return convertToResponseDTO(stock);
    }

    // Yeni malzeme oluştur
    public StockResponseDTO createStock(StockRequestDTO requestDTO) {
        stockValidator.validateStockRequest(requestDTO);

        String nameToCheck = normalizedName(requestDTO.getName());
        if (nameToCheck != null && stockRepository.existsByName(nameToCheck)) {
            throw new StockConflictException("Aynı isimde malzeme zaten mevcut: " + nameToCheck);
        }

        Stock stock = convertToEntity(requestDTO);
        Stock saved = stockRepository.save(stock);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Stock created: " + saved.getName(),
                    "stockName", saved.getName(),
                    "quantity", String.valueOf(saved.getQuantity()),
                    "unit", saved.getUnit() != null ? saved.getUnit().name() : null  // enum -> String
            );
            activityLogService.logActivity("CREATE", "STOCK", saved.getId(), details);
        } catch (Exception ignored) { }

        return convertToResponseDTO(saved);
    }

    // Var olan malzemeyi güncelle
    public StockResponseDTO updateStock(Long id, StockRequestDTO requestDTO) {
        Stock existing = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock not found, ID: " + id));

        stockValidator.validateStockRequest(requestDTO);

        String newName = normalizedName(requestDTO.getName());
        if (newName != null) {
            Stock byName = stockRepository.findByName(newName);
            if (byName != null && !byName.getId().equals(id)) {
                throw new StockConflictException("Aynı isimde başka bir malzeme mevcut: " + newName);
            }
        }

        existing.setName(requestDTO.getName());
        existing.setUnit(requestDTO.getUnit()); // enum set
        existing.setQuantity(requestDTO.getStockQuantity() != null ? requestDTO.getStockQuantity() : existing.getQuantity());
        existing.setMinQuantity(requestDTO.getMinQuantity() != null ? requestDTO.getMinQuantity() : existing.getMinQuantity());

        Stock updated = stockRepository.save(existing);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Stock updated: " + updated.getName(),
                    "stockName", updated.getName(),
                    "quantity", String.valueOf(updated.getQuantity()),
                    "minQuantity", String.valueOf(updated.getMinQuantity()),

                    "unit", updated.getUnit() != null ? updated.getUnit().name() : null
            );
            activityLogService.logActivity("UPDATE", "STOCK", updated.getId(), details);
        } catch (Exception ignored) { }

        return convertToResponseDTO(updated);
    }

    public StockResponseDTO updateMinQuantity(Long id, BigDecimal minQuantity) {
        Stock existing = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock not found, ID: " + id));

        if (minQuantity == null || BDH.isNegative(minQuantity)) {
            throw new IllegalArgumentException("Minimum quantity must be zero or greater");
        }

        existing.setMinQuantity(minQuantity);
        Stock updated = stockRepository.save(existing);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Stock minQuantity updated: " + updated.getName(),
                    "stockName", updated.getName(),
                    "newMinQuantity", String.valueOf(updated.getMinQuantity())
            );
            activityLogService.logActivity("UPDATE_MIN_QTY", "STOCK", updated.getId(), details);
        } catch (Exception ignored) {}

        return convertToResponseDTO(updated);
    }


    // Malzemeyi sil
    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new StockNotFoundException("Stock not found, ID: " + id);
        }

        Stock stock = stockRepository.findById(id).orElse(null);

        stockRepository.deleteById(id);

        if (stock != null) {
            try {
                ObjectNode details = activityLogService.createDetailsNode(
                        "Stock deleted: " + stock.getName(),
                        "stockName", stock.getName(),
                        "quantity", String.valueOf(stock.getQuantity()),
                        "unit", stock.getUnit() != null ? stock.getUnit().name() : null
                );
                activityLogService.logActivity("DELETE", "STOCK", id, details);
            } catch (Exception ignored) { }
        }
    }
}
