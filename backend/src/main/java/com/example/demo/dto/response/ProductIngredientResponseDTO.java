// src/main/java/com/example/demo/dto/ProductIngredientResponseDTO.java
package com.example.demo.dto.response;

import com.example.demo.model.ProductIngredientId;
import java.math.BigDecimal;

public class ProductIngredientResponseDTO  {
    private ProductIngredientId id;
    private ProductResponseDto product; // Changed from ProductDTO to ProductResponseDto
    private StockResponseDTO ingredient; // Changed from IngredientDTO to StockResponseDTO
    private BigDecimal quantityPerUnit;

    // Constructors
    public ProductIngredientResponseDTO() {
    }

    public ProductIngredientResponseDTO(ProductIngredientId id, ProductResponseDto product, StockResponseDTO ingredient, BigDecimal quantityPerUnit) {
        this.id = id;
        this.product = product;
        this.ingredient = ingredient;
        this.quantityPerUnit = quantityPerUnit;
    }

    // Getters and Setters
    public ProductIngredientId getId() {
        return id;
    }

    public void setId(ProductIngredientId id) {
        this.id = id;
    }

    public ProductResponseDto getProduct() {
        return product;
    }

    public void setProduct(ProductResponseDto product) {
        this.product = product;
    }

    public StockResponseDTO getIngredient() {
        return ingredient;
    }

    public void setIngredient(StockResponseDTO ingredient) {
        this.ingredient = ingredient;
    }

    public BigDecimal getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(BigDecimal quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }
}