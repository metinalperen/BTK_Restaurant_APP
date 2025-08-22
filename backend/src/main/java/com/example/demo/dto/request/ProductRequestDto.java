package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductRequestDto {
    @Schema(description = "Ürün adı", example = "Kahve", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "Ürün açıklaması", example = "Taze çekilmiş kahve", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @Schema(description = "Ürün fiyatı", example = "15.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;
    @Schema(description = "Ürün aktif mi?", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;
    @Schema(description = "Ürün kategorisi", example = "İçecek", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    public ProductRequestDto() {
    }

    public ProductRequestDto(String name, String description, BigDecimal price, Boolean isActive, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.category = category;
    }

}
