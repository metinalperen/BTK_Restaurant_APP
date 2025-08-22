package com.example.demo.dto.request;

import com.example.demo.enums.StockUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockRequestDTO {

    @Schema(description = "Stock name", example = "Sugar", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Schema(
            description = "Unit of measurement",
            example = "KILOGRAM",
            allowableValues = {"LITRE", "KILOGRAM", "PIECE"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Unit cannot be null")
    private StockUnit unit;

    @Schema(description = "Stock quantity", example = "1000.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal stockQuantity;

    @Schema(description = "Minimum stock quantity", example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Minimum stock quantity cannot be null")
    @Positive(message = "Minimum stock quantity must be positive")
    private BigDecimal minQuantity;


    public StockRequestDTO() {}

    public StockRequestDTO(String name, StockUnit unit, BigDecimal stockQuantity) {
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
    }

}
