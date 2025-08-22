// src/main/java/com/example/productingredients/repository/ProductIngredientRepository.java
package com.example.demo.repository;

import com.example.demo.model.ProductIngredient;
import com.example.demo.model.ProductIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, ProductIngredientId> {

    // You can add custom query methods here if needed
    // For example, finding product ingredients by product ID
    List<ProductIngredient> findByProductId(Long productId);

    // Or finding by ingredient ID
    List<ProductIngredient> findByStockId(Long stockId);

    // Or finding a specific product ingredient by product and ingredient
    Optional<ProductIngredient> findByProductIdAndStockId(Long productId, Long stockId);
}