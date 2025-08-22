package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.persistence.MapsId;

import java.math.BigDecimal;

@Entity
@Table(name = "product_ingredients")
public class ProductIngredient {

    @EmbeddedId
    private ProductIngredientId id = new ProductIngredientId();

    // Many ProductIngredients can be associated with one Product
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading for performance
    @MapsId("productId")
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product; // Changed type from Product to Product

    // Many ProductIngredients can use one Stock
    @ManyToOne(fetch = FetchType.EAGER) // Lazy loading for performance
    @MapsId("stockId")
    @JoinColumn(name = "stock_id", referencedColumnName = "id")
    private Stock stock; // Changed type from Stock to Stock

    @Column(name = "quantity_per_unit", precision = 10, scale = 4) // Adjust precision/scale as needed
    private BigDecimal quantityPerUnit;

    // Constructors
    public ProductIngredient() {}

    public ProductIngredient(Product product, Stock stock, BigDecimal quantityPerUnit) { // Changed types in constructor
        this.product = product;
        this.stock = stock;
        this.quantityPerUnit = quantityPerUnit;
    }

    // Getters and Setters
    public ProductIngredientId getId() {
        return id;
    }

    public void setId(ProductIngredientId id) {
        this.id = id;
    }

    public Product getProduct() { // Changed getter name to getProducts
        return product;
    }

    public void setProduct(Product product) { // Changed setter name to setProducts
        this.product = product;
    }

    public Stock getIngredient() { // Changed getter name to getIngredients
        return stock;
    }

    public void setIngredient(Stock stock) { // Changed setter name to setIngredients
        this.stock = stock;
    }

    public BigDecimal getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(BigDecimal quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }

    @Override
    public String toString() {
        return "ProductIngredient{" +
                "id=" + id +
                ", productId=" + (product != null ? product.getId() : "null") +
                ", ingredientId=" + (stock != null ? stock.getId() : "null") +
                ", quantityPerUnit=" + quantityPerUnit +
                '}';
    }
}