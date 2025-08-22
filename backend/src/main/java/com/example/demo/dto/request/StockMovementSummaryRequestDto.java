package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class StockMovementSummaryRequestDto {
    @Schema(description = "Ürün ID'si", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long stockId;

    @Schema(description = "Hareket tarihi", example = "2023-10-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate movementDate;

    @Schema(description = "Miktar", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}


