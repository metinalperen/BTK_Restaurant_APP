package com.example.demo.dto.response;

import java.time.LocalDate;

public class StockMovementSummaryResponseDto {
    private Long id;
    private Long stockId;
    private LocalDate movementDate;
    private Integer quantity;  // Integer olarak düzeltildi

    // getter & setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStockId() { return stockId; }
    public void setStockId(Long stockId) { this.stockId = stockId; }

    public LocalDate getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDate movementDate) { this.movementDate = movementDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
