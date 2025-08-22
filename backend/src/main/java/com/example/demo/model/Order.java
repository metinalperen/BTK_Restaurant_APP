package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Siparişi alan kullanıcı
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Masaya ait sipariş
    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false) //10 basamağa kadar price tutulabilir. virgülden sonra 4 basamak.
    private BigDecimal totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Veritabanına sütunu eklendi.
    // Ödeme alındı mı? (boolen tutuldu)
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    // Veritabanına sütunu eklendi.
    //Siparişin son güncellendiği zaman
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Veritabanına sütunu eklendi.
    // Optimistic locking -> Aynı anda birden fazla garson aynı masada güncelleme yapıp çakışma olmasın.
    @Version
    private Long version;

    // Siparişteki ürün kalemleri
    @JsonManagedReference(value = "order-items")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)// Tam senkron update'te listeden çıkanları otomatik silmek için orphanRemoval = true
    private List<OrderItem> items = new ArrayList<>();

    // --- Getter/Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public DiningTable getTable() { return table; }
    public void setTable(DiningTable table) { this.table = table; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        // Mutate in place to preserve Hibernate's PersistentCollection reference
        this.items.clear();
        if (items != null) {
            for (OrderItem oi : items) {
                if (oi != null) {
                    oi.setOrder(this); // maintain bidirectional link
                    this.items.add(oi);
                }
            }
        }
        // keep total consistent
        recalcTotal();
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        recalcTotal(); // ilk kayıt öncesi
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        recalcTotal(); // her update'te toplamı tazele
    }

    //Kalemlerin totalPrice'larını toplayarak sipariş toplamını hesaplar.
    //Service katmanı zaten unitPrice ve totalPrice'ı setleyecek; burada güvenlik için tekrar topluyoruz.
    public void recalcTotal() {
        if (items == null || items.isEmpty()) {
            this.totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return;
        }
        this.totalPrice = items.stream()
                .map(oi -> oi.getTotalPrice() != null ? oi.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalAmount() {
        return (totalPrice != null) ? totalPrice.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}

