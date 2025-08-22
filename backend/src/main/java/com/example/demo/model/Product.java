package com.example.demo.model;

import com.example.demo.enums.ItemCategory;
import com.example.demo.enums.ItemCategoryConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(precision = 10, scale = 2, nullable = false) //Null pointer hatası almamak için nullable=false eklendi.
    private BigDecimal price;

    private Boolean isActive;

    @Convert(converter = ItemCategoryConverter.class)
    private ItemCategory category;

    // Constructor
    public Product() {
    }

    public Product(String name, String description, BigDecimal price, Long stockQuantity, Boolean isActive, ItemCategory category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.category = category;
    }

    // Getter ve Setterlar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }
}
