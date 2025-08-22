package com.example.demo.repository;

import com.example.demo.model.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Long> {
    Optional<DailySalesSummary> findByReportDate(LocalDate date);
    
    Optional<DailySalesSummary> findByReportDateAndReportType(LocalDate reportDate, String reportType);
    
    List<DailySalesSummary> findByReportDateBetweenOrderByReportDateDesc(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT d FROM DailySalesSummary d ORDER BY d.reportDate DESC")
    List<DailySalesSummary> findAllOrderByReportDateDesc();
    
    List<DailySalesSummary> findByReportTypeOrderByReportDateDesc(String reportType);
}