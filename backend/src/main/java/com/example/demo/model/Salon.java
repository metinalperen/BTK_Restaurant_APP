
package com.example.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

    @Entity
    @Table(name = "salons")
    @Getter
    @ToString(exclude = {"diningTables"})
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public class Salon {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name", nullable = false)
        @Setter
        private String name;

        @Column(name = "description")
        @Setter
        private String description;

        @Column(name = "capacity", nullable = false)
        private Integer capacity = 0;

        // Toplam masa sayısı (DB'de tutulacak)
        @Column(name = "total_tables", nullable = false)
        private Integer totalTables = 0;

        @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "salon"})
        private Set<DiningTable> diningTables = new HashSet<>();

        // capacity ve totalTables dışarıdan set edilmesin diye setter kaldırıldı.
        // Sadece name ve description için setter var.

          public void setCapacity(Integer capacity) {
            this.capacity = capacity;
          }
        // Özel güncelleme metodu

        private void updateStats() {
            totalTables = diningTables.size();
            capacity = diningTables.stream()
                    .mapToInt(DiningTable::getCapacity)
                    .sum();
        }

        // Masa ekleme metodu
        public void addDiningTable(DiningTable table) {
            diningTables.add(table);
            table.setSalon(this);
            updateStats();
        }

        // Masa silme metodu
        public void removeDiningTable(DiningTable table) {
            diningTables.remove(table);
            table.setSalon(null);
            updateStats();
        }

        // Kapasiteyi dönen getter
        public int getCapacity() {
            return capacity;
        }

        // Toplam masa sayısını dönen getter
        public int getTotalTables() {
            return totalTables;
        }

    }
