package com.example.demo.dto.request;

import com.example.demo.enums.StockMovementEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequestDTO {

    @NotNull(message = "Product ID boş olamaz")
    @Min(value = 1, message = "Product ID 1'den küçük olamaz")
    private Long stockId;

    @NotNull(message = "Değişim miktarı boş olamaz")
    @Min(value = -1000, message = "Değişim miktarı -1000'den küçük olamaz")
    @Max(value = 1000, message = "Değişim miktarı 1000'den büyük olamaz")
    private BigDecimal change;

    @NotNull(message = "Sebep boş olamaz")
    private StockMovementEnum reason;

    private String note; // opsiyonel açıklama
}