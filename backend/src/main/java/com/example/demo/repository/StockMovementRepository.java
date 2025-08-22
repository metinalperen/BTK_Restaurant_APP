package com.example.demo.repository;

import com.example.demo.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByStockIdOrderByTimestampDesc(Long stockId);

    List<StockMovement> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);

    List<StockMovement> findByStockIdAndTimestampBetweenOrderByTimestampDesc(Long stockId, LocalDateTime startDate, LocalDateTime endDate);

    List<StockMovement> findByReasonOrderByTimestampDesc(String reason);

    @Query("SELECT SUM(sm.change) FROM StockMovement sm WHERE sm.stock.id = :stockId")
    Integer getTotalStockChangeByStockId(@Param("stockId") Long stockId);
}