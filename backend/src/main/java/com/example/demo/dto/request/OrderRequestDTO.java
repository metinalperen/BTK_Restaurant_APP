package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class OrderRequestDTO {

    @Schema(description = "Kullanıcı ID'si", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private int userId;
    @Schema(description = "Masa ID'si", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private int tableId;
    @Schema(description = "Sipariş öğeleri", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequestDTO> items;

    // --- Getter & Setter ---
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public List<OrderItemRequestDTO> getItems() { return items; }
    public void setItems(List<OrderItemRequestDTO> items) { this.items = items; }
}
