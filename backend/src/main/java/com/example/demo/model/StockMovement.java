package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Stock bilgisi zorunludur")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", referencedColumnName = "id", nullable = false)
    private Stock stock;

    @NotNull(message = "Miktar değişimi zorunludur")
    @Column(name = "change", nullable = false, precision = 10, scale = 4)
    private BigDecimal change; // Pozitif: stok artışı, Negatif: stok azalışı

    @NotNull(message = "Sebep zorunludur")
    @Pattern(regexp = "PURCHASE|ORDER|ADJUSTMENT|WASTE|RETURN", message = "Geçersiz stok hareket nedeni")
    @Column(name = "reason", nullable = false)
    private String reason; // "PURCHASE", "ORDER", "ADJUSTMENT", "WASTE", "RETURN"

    @Size(max = 500, message = "Not 500 karakterden uzun olamaz")
    @Column(name = "note")
    private String note; // Opsiyonel açıklama

    @NotNull(message = "Zaman damgası zorunludur")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Constructor for creating new stock movements
    public StockMovement(Stock stock, BigDecimal change, String reason) {
        this.stock = stock;
        this.change = change;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for creating stock movements with an optional note
    public StockMovement(Stock stock, BigDecimal change, String reason, String note) {
        this(stock, change, reason);
        this.note = note;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) this.timestamp = LocalDateTime.now();
        normalize();
    }

    @PreUpdate
    public void preUpdate() {
        normalize();
    }

    private void normalize() {
        if (this.reason != null) this.reason = this.reason.trim().toUpperCase();
        if (this.note != null) this.note = this.note.trim().replaceAll("\\s+", " ");
    }
}