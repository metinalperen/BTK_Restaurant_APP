package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    // --- Getter/Setter ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi siparişin parçası
    @JsonBackReference(value = "order-items")
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Hangi ürün
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    // Snapshot fiyat
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "note", length = 500)
    private String note;

    @PrePersist
    @PreUpdate
    public void calcSubtotal() {
        // Not: Unit price service katmanında setlenmeli (Product.price'tan snapshot).
        // Burada sadece güvenlik için hesapladım.
        if (unitPrice == null) {
            // Service doğru setlemediyse hatayı erken yakalamak daha iyi (sessizce 0'a düşürmüyoruz)
            throw new IllegalStateException("OrderItem.unitPrice null olamaz. Service katmanında setlenmeli.");
        }
        if (quantity < 0) {
            throw new IllegalStateException("OrderItem.quantity negatif olamaz.");
        }
        this.totalPrice = unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
