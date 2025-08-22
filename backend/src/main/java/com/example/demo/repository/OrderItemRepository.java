package com.example.demo.repository;

import com.example.demo.model.OrderItem;
import com.example.demo.repository.projection.TopProductView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long order_id);
    boolean existsByProductId(Long productId);
    
    // Alternative method for explicit deletion of order items
    void deleteByOrderId(Long orderId);

    /**
     * Belirli tarih aralığında en çok satan ürünleri (adet bazında) döndürür.
     * ORDER BY SUM(oi.quantity) DESC ile sıralanır.
     * Pageable ile LIMIT uygulanır.
     */
    @Query("SELECT p.id AS productId, p.name AS productName, " +
           "SUM(oi.quantity) AS totalQuantity, COUNT(DISTINCT o.id) AS orderCount, " +
           "SUM(oi.totalPrice) AS totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o JOIN oi.product p " +
           "WHERE o.createdAt >= :start AND o.createdAt <= :end " +
           "GROUP BY p.id, p.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<TopProductView> findTopProductsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order IS NOT NULL")
    long countByOrderIsNotNull();
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product IS NOT NULL")
    long countByProductIsNotNull();
    
    @Query("SELECT p.id AS productId, p.name AS productName, " +
           "SUM(oi.quantity) AS totalQuantity, COUNT(DISTINCT o.id) AS orderCount, " +
           "SUM(oi.totalPrice) AS totalRevenue " +
           "FROM OrderItem oi JOIN oi.order o JOIN oi.product p " +
           "GROUP BY p.id, p.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<TopProductView> findAllTopProducts(Pageable pageable);
}
