package com.example.demo.dto.response;

import com.example.demo.enums.StockUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockResponseDTO {

    private Long id;
    private String name;
    private StockUnit unit;

    @NotNull
    @PositiveOrZero
    private  BigDecimal stockQuantity;
    private BigDecimal minQuantity;

    public StockResponseDTO() {}

    public StockResponseDTO(Long id, String name, StockUnit unit, BigDecimal stockQuantity) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
    }

}
