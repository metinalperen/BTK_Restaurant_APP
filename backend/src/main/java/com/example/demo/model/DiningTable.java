package com.example.demo.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "dining_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"orders", "reservations"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", unique = true)
    private Integer tableNumber;

    // Durum bilgisini ayrı bir tablodaki nesneye bağladık.
    // Bu, "status" alanının artık bir id olarak tutulmasını sağlar.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TableStatus status;

    @Column(name = "capacity")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salon_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "diningTables"})
    private Salon salon;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "table"})
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "table"})
    private Set<Reservation> reservations = new HashSet<>();

    public boolean isOccupied() {
        return status != null && "OCCUPIED".equalsIgnoreCase(status.getName());
    }

    /**
     * Masanın order durumuna göre gerçek durumunu döner.
     * Order varsa ve is_completed=false ise kırmızı (OCCUPIED)
     * Order varsa ve is_completed=true ise yeşil (AVAILABLE) 
     * Order yoksa mevcut status'ü döner
     */
    public boolean hasActiveOrder() {
        return orders != null && orders.stream()
                .anyMatch(order -> !order.isCompleted());
    }

    public boolean hasCompletedOrder() {
        return orders != null && orders.stream()
                .anyMatch(order -> order.isCompleted());
    }

    /**
     * Aktif order'daki toplam item sayısını döner
     */
    public int getActiveOrderItemsCount() {
        if (orders == null) return 0;
        
        return orders.stream()
                .filter(order -> !order.isCompleted()) // Sadece aktif orderlar (is_completed = false)
                .mapToInt(order -> order.getItems() != null ? order.getItems().size() : 0)
                .sum();
    }
}