package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

/**
 * Masa durumlarını (örneğin: MÜSAİT, DOLU, REZERVE) yöneten entity sınıfı.
 * Bu sınıf, DiningTable'ın durum bilgisini ayrı bir tablo olarak saklar.
 */
@Entity
@Table(name = "table_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "diningTables")
public class TableStatus {

    // Birincil anahtar (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Durum adını tutar (örneğin: "AVAILABLE", "OCCUPIED").
    // unique = true ile aynı durumda birden fazla kayıt olmasını engeller.
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Bu durumun hangi masalara ait olduğunu gösterir.
    // Bir durumun birden fazla masası olabilir (One-to-Many).
    // mappedBy = "status", ilişkinin DiningTable tarafında yönetildiğini belirtir.
    @OneToMany(mappedBy = "status")
    private Set<DiningTable> diningTables = new HashSet<>();
}
