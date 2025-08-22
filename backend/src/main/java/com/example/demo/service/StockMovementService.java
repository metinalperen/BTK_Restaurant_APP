package com.example.demo.service;

import com.example.demo.dto.request.StockMovementRequestDTO;
import com.example.demo.dto.response.StockMovementResponseDTO;
import com.example.demo.enums.StockMovementEnum;
import com.example.demo.exception.stock.StockNotFoundException;
import com.example.demo.exception.stockmovement.StockMovementConflictException;
import com.example.demo.exception.stockmovement.StockMovementNotFoundException;
import com.example.demo.model.Stock;
import com.example.demo.model.StockMovement;
import com.example.demo.repository.StockMovementRepository;
import com.example.demo.repository.StockRepository;
import com.example.demo.utils.BDH;
import com.example.demo.validation.StockMovementValidator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockRepository stockRepository;
    private final StockMovementValidator validator;
    private final ActivityLogService activityLogService;

    public StockMovementResponseDTO createStockMovement(StockMovementRequestDTO requestDTO) {
        validator.validateStockMovement(requestDTO);

        Stock stock = stockRepository.findById(requestDTO.getStockId())
                .orElseThrow(() -> new StockNotFoundException("Stock not found with id: " + requestDTO.getStockId()));

        BigDecimal change = requestDTO.getChange();
        BigDecimal newQuantity = BDH.add(stock.getQuantity(),change);
        if (BDH.isNegative(newQuantity)) {
            throw new StockMovementConflictException("Bu hareket stok miktarını negatif yapar. Mevcut: " + stock.getQuantity() + ", değişim: " + change);
        }

        stock.setQuantity(newQuantity);
        stockRepository.save(stock);

        StockMovement stockMovement = new StockMovement(
                stock,
                change,
                requestDTO.getReason().name(),
                requestDTO.getNote()
        );

        StockMovement saved = stockMovementRepository.save(stockMovement);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Stock movement created",
                    "stockId", stock.getId().toString(),
                    "stockName", stock.getName(),
                    "change", String.valueOf(change),
                    "reason", requestDTO.getReason().name(),
                    "newQuantity", String.valueOf(newQuantity)
            );
            activityLogService.logActivity("CREATE", "STOCK_MOVEMENT", saved.getId(), details);
        } catch (Exception ignored) { }

        return convertToResponseDTO(saved);
    }

    public List<StockMovementResponseDTO> getAllStockMovements() {
        return stockMovementRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public StockMovementResponseDTO getStockMovementById(Long id) {
        StockMovement stockMovement = stockMovementRepository.findById(id)
                .orElseThrow(() -> StockMovementNotFoundException.forId(id));
        return convertToResponseDTO(stockMovement);
    }

    public List<StockMovementResponseDTO> getStockMovementsByStockId(Long stockId) {
        return stockMovementRepository.findByStockIdOrderByTimestampDesc(stockId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<StockMovementResponseDTO> getStockMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return stockMovementRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<StockMovementResponseDTO> getStockMovementsByReason(StockMovementEnum reason) {
        return stockMovementRepository.findByReasonOrderByTimestampDesc(reason.name()).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Integer getTotalStockChangeByStockId(Long stockId) {
        Integer totalChange = stockMovementRepository.getTotalStockChangeByStockId(stockId);
        return totalChange != null ? totalChange : 0;
    }

    public StockMovementResponseDTO updateStockMovement(Long id, StockMovementRequestDTO requestDTO) {
        StockMovement existing = stockMovementRepository.findById(id)
                .orElseThrow(() -> StockMovementNotFoundException.forId(id));

        validator.validateStockMovement(requestDTO);

        Stock oldStock = existing.getStock();
        BigDecimal oldChange = existing.getChange();

        Stock newStock = oldStock;
        if (!oldStock.getId().equals(requestDTO.getStockId())) {
            newStock = stockRepository.findById(requestDTO.getStockId())
                    .orElseThrow(() -> new StockNotFoundException("Stock not found with id: " + requestDTO.getStockId()));
        }

        // Revert old change on old stock
        BigDecimal revertedQty = BDH.subtract(oldStock.getQuantity(),oldChange);
        if (BDH.isNegative(revertedQty)) {
            throw new StockMovementConflictException("Eski stok hareketi geri alınamıyor. Mevcut: " + oldStock.getQuantity() + ", değişim: " + oldChange);
        }
        oldStock.setQuantity(revertedQty);

        // Apply new change on new stock
        BigDecimal newChange = requestDTO.getChange();
        BigDecimal baseQty = newStock.getQuantity();

        // If switching stocks, baseQty should be from newStock after potential unrelated changes; already loaded
        BigDecimal appliedQty = BDH.add(baseQty,newChange);
        if (BDH.isNegative(appliedQty)) {
            throw new StockMovementConflictException("Yeni stok hareketi stok miktarını negatif yapar. Mevcut: " + baseQty + ", değişim: " + newChange);
        }

        newStock.setQuantity(appliedQty);

        // Persist stocks
        stockRepository.save(oldStock);
        if (!newStock.getId().equals(oldStock.getId())) {
            stockRepository.save(newStock);
        }

        // Update movement entity
        existing.setStock(newStock);
        existing.setChange(newChange);
        existing.setReason(requestDTO.getReason().name());
        existing.setNote(requestDTO.getNote());
        existing.setTimestamp(LocalDateTime.now());

        StockMovement updated = stockMovementRepository.save(existing);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Stock movement updated",
                    "movementId", updated.getId().toString(),
                    "oldStockId", oldStock.getId().toString(),
                    "newStockId", newStock.getId().toString(),
                    "oldChange", String.valueOf(oldChange),
                    "newChange", String.valueOf(newChange),
                    "reason", requestDTO.getReason().name(),
                    "newQuantityOnNewStock", String.valueOf(appliedQty)
            );
            activityLogService.logActivity("UPDATE", "STOCK_MOVEMENT", updated.getId(), details);
        } catch (Exception ignored) { }

        return convertToResponseDTO(updated);
    }

    public void deleteStockMovement(Long id) {
        StockMovement existing = stockMovementRepository.findById(id)
                .orElseThrow(() -> StockMovementNotFoundException.forId(id));

        Stock stock = existing.getStock();
        BigDecimal change = existing.getChange();

        BigDecimal newQty = BDH.subtract(stock.getQuantity(),change);
        if (BDH.isNegative(newQty)) {
            throw new StockMovementConflictException("Stok hareketi silinirse stok miktarı negatif olur. Mevcut: " + stock.getQuantity() + ", değişim: " + change);
        }
        stock.setQuantity(newQty);
        stockRepository.save(stock);

        stockMovementRepository.delete(existing);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Stock movement deleted",
                    "movementId", id.toString(),
                    "stockId", stock.getId().toString(),
                    "revertedChange", String.valueOf(change),
                    "newQuantity", String.valueOf(newQty)
            );
            activityLogService.logActivity("DELETE", "STOCK_MOVEMENT", id, details);
        } catch (Exception ignored) { }
    }

    private StockMovementResponseDTO convertToResponseDTO(StockMovement stockMovement) {
        return new StockMovementResponseDTO(
                stockMovement.getId(),
                stockMovement.getStock().getId(),
                stockMovement.getChange(),
                StockMovementEnum.valueOf(stockMovement.getReason()),
                stockMovement.getNote(),
                stockMovement.getTimestamp()
        );
    }
}