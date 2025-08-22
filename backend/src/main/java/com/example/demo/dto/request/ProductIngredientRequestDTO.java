package com.example.demo.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductIngredientRequestDTO {

    @Schema(description = "ID of the product", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "ID of the ingredient", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Stock ID cannot be null")
    @Positive(message = "Stock ID must be a positive number")
    private Long ingredientId;

    @Schema(description = "Quantity of the ingredient per unit of product", example = "0.5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity per unit cannot be null")
    @Positive(message = "Quantity per unit must be a positive number")
    private BigDecimal quantityPerUnit;

    // Constructors
    public ProductIngredientRequestDTO() {
    }

    public ProductIngredientRequestDTO(Long productId, Long ingredientId, BigDecimal quantityPerUnit) {
        this.productId = productId;
        this.ingredientId = ingredientId;
        this.quantityPerUnit = quantityPerUnit;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public BigDecimal getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(BigDecimal quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }
}