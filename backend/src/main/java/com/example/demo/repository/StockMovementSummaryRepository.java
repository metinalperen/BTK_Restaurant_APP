package com.example.demo.repository;

import com.example.demo.model.StockMovementSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementSummaryRepository extends JpaRepository<StockMovementSummary, Long> {

    // Find by stock ID
    List<StockMovementSummary> findByStockIdOrderByMovementDateDesc(Long stockId);

    // Find by date range
    List<StockMovementSummary> findByMovementDateBetweenOrderByMovementDateDesc(LocalDate startDate, LocalDate endDate);

    // Find by stock and date range
    List<StockMovementSummary> findByStockIdAndMovementDateBetweenOrderByMovementDateDesc(
        Long stockId, LocalDate startDate, LocalDate endDate);

    // Find by specific date
    List<StockMovementSummary> findByMovementDateOrderByStockId(LocalDate movementDate);

    // Find by stock and specific date
    Optional<StockMovementSummary> findByStockIdAndMovementDate(Long stockId, LocalDate movementDate);

    // Get total quantity by stock ID
    @Query("SELECT SUM(sms.quantity) FROM StockMovementSummary sms WHERE sms.stock.id = :stockId")
    Integer getTotalQuantityByStockId(@Param("stockId") Long stockId);

    // Get total quantity for date range
    @Query("SELECT SUM(sms.quantity) FROM StockMovementSummary sms WHERE sms.movementDate BETWEEN :startDate AND :endDate")
    Integer getTotalQuantityByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Check if summary exists for stock and date
    boolean existsByStockIdAndMovementDate(Long stockId, LocalDate movementDate);
}
