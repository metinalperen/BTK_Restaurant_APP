package com.example.demo.dto.response;

import com.example.demo.enums.StockMovementEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockMovementResponseDTO {
    private Long id;
    private Long stockId;
    private BigDecimal change;
    private StockMovementEnum reason;
    private String note;
    private LocalDateTime timestamp;

    public StockMovementResponseDTO() {}

    public StockMovementResponseDTO(Long id, Long stockId, BigDecimal change, StockMovementEnum reason, String note, LocalDateTime timestamp) {
        this.id = id;
        this.stockId = stockId;
        this.change = change;
        this.reason = reason;
        this.note = note;
        this.timestamp = timestamp;
    }
}