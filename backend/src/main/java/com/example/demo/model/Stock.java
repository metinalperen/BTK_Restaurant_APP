package com.example.demo.model;

import com.example.demo.enums.StockUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Malzeme adı boş olamaz")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Ölçü birimi boş olamaz")
    @Enumerated(EnumType.STRING)                 // <--- enum string olarak saklanır
    @Column(nullable = false)
    private StockUnit unit;

    @NotNull(message = "Stok miktarı boş olamaz")
    @PositiveOrZero(message = "Stok miktarı 0 veya daha büyük olmalıdır")
    @Column(name = "stock_quantity", nullable = false,precision = 10, scale = 4)
    private BigDecimal quantity;

    @NotNull(message = "Minimum stok miktarı boş olamaz")
    @PositiveOrZero(message = "Minimum stok miktarı 0 veya daha büyük olmalıdır")
    @Column(name = "min_quantity", nullable = false,precision = 10, scale = 4)
    private BigDecimal minQuantity;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (name != null) name = name.trim().replaceAll("\\s+", " ");
    }
}
