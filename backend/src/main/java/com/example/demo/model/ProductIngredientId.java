package com.example.demo.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductIngredientId implements Serializable {

    private Long productId;
    private Long stockId;

    public ProductIngredientId() {}

    public ProductIngredientId(Long productId, Long stockId) {
        this.productId = productId;
        this.stockId = stockId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductIngredientId that = (ProductIngredientId) o;
        return Objects.equals(productId, that.productId) &&
               Objects.equals(stockId, that.stockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, stockId);
    }
}
