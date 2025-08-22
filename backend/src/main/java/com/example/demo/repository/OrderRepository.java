package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Garson (user) bazlı siparişler
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Masa bazlı
    List<Order> findByTableId(Long tableId);
    
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = :orderDate")
    List<Order> findByCreatedAt(@Param("orderDate") LocalDate orderDate);
    
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Masa için tek açık sipariş (EAGER fetch için EntityGraph ile item ve product’ları da çekti)
    @EntityGraph(attributePaths = {"items", "items.product", "table"})
    Optional<Order> findFirstByTableIdAndIsCompletedFalse(Long tableId);

    // Kasiyer listeleri için pratik
    @EntityGraph(attributePaths = {"items", "items.product", "table"})
    List<Order> findByIsCompleted(boolean isCompleted);

    @EntityGraph(attributePaths = {"items", "items.product", "table"})
    List<Order> findByIsCompletedAndTableId(boolean isCompleted, Long tableId);

}
