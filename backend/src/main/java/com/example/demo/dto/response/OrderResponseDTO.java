package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class OrderResponseDTO {

    // --- Getter & Setter ---
    @Getter
    private Long orderId;
    @Getter
    private Long userId;
    @Getter
    private String waiterName;
    private Long tableId;
    private Integer tableNumber;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
    private Boolean completed;

    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItemResponseDTO> getItems() { return items; }
    public void setItems(List<OrderItemResponseDTO> items) { this.items = items; }

    @JsonProperty("isCompleted")
    public Boolean isCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    @Override
    public String toString() {
        return "OrderResponseDTO{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", waiterName='" + waiterName + '\'' +
                ", tableId=" + tableId +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", completed=" + completed +
                ", items=" + items +
                '}';
    }
}
