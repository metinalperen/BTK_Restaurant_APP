package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.enums.ItemCategory; // ← BU SATIRI EKLE
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(ItemCategory category);

    List<Product> findByIsActiveTrue();

    List<Product> findByPriceLessThanEqual(BigDecimal price);

    List<Product> findByNameContainingIgnoreCase(String name);

    Optional<Product> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);


}
