package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "stock_movement_summary")
@Getter
@Setter
@NoArgsConstructor
public class StockMovementSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", referencedColumnName = "id", nullable = false)
    private Stock stock;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Constructor for easy creation
    public StockMovementSummary(Stock stock, LocalDate movementDate, Integer quantity) {
        this.stock = stock;
        this.movementDate = movementDate;
        this.quantity = quantity;
    }
}
