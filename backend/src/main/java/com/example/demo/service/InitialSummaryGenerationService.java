package com.example.demo.service;

import com.example.demo.model.DailySalesSummary;
import com.example.demo.repository.DailySalesSummaryRepository;
import com.example.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;

/**
 * Service to generate initial summaries when the application starts.
 * This ensures real-time analytics work immediately without manual generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InitialSummaryGenerationService {

    private final DailySalesSummaryRepository dailySalesSummaryRepository;
    private final DailySalesSummaryJobService dailySalesSummaryJobService;
    private final OrderRepository orderRepository;

    /**
     * 🔥 REAL-TIME ANALYTICS: Generate initial summaries on application startup
     * This ensures the real-time system has data to work with immediately
     */
    @EventListener(ApplicationReadyEvent.class)
    public void generateInitialSummaries() {
        log.info("Generating initial summaries for real-time analytics...");
        
        try {
            // Check if we have any existing summaries
            long existingCount = dailySalesSummaryRepository.count();
            if (existingCount > 0) {
                log.info("Found {} existing summaries, checking if current period summaries are available", existingCount);
            }

            // Generate summaries for current periods (for real-time analytics)
            LocalDate today = LocalDate.now();
            
            // Generate daily summary for today (current period)
            if (!hasSummaryForDateAndType(today, "DAILY")) {
                dailySalesSummaryJobService.generateSalesSummary(today, today, "DAILY");
                log.info("Generated current daily summary for {}", today);
            }
            
            // Generate weekly summary for current week (current period)
            LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate weekStart = weekEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (!hasSummaryForDateAndType(weekEnd, "WEEKLY")) {
                dailySalesSummaryJobService.generateSalesSummary(weekStart, weekEnd, "WEEKLY");
                log.info("Generated current weekly summary for week {} to {}", weekStart, weekEnd);
            }
            
            // Generate monthly summary for current month (current period)
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            LocalDate monthStart = monthEnd.withDayOfMonth(1);
            if (!hasSummaryForDateAndType(monthEnd, "MONTHLY")) {
                dailySalesSummaryJobService.generateSalesSummary(monthStart, monthEnd, "MONTHLY");
                log.info("Generated current monthly summary for month {} to {}", monthStart, monthEnd);
            }
            
            // Generate yearly summary for current year (current period)
            LocalDate yearEnd = today.withDayOfYear(today.lengthOfYear());
            LocalDate yearStart = yearEnd.withDayOfYear(1);
            if (!hasSummaryForDateAndType(yearEnd, "YEARLY")) {
                dailySalesSummaryJobService.generateSalesSummary(yearStart, yearEnd, "YEARLY");
                log.info("Generated current yearly summary for year {} to {}", yearStart, yearEnd);
            }

            // Also generate historical summaries if they don't exist and have orders
            if (existingCount == 0) {
                log.info("No existing summaries found, generating historical summaries as well...");
                
                // Generate daily summary for yesterday (if orders exist)
                LocalDate yesterday = today.minusDays(1);
                if (hasOrdersForDate(yesterday)) {
                    dailySalesSummaryJobService.generateSalesSummary(yesterday, yesterday, "DAILY");
                    log.info("Generated historical daily summary for {}", yesterday);
                }

                // Generate weekly summary for previous week (if orders exist)
                LocalDate prevWeekStart = weekStart.minusWeeks(1);
                LocalDate prevWeekEnd = weekStart.minusDays(1);
                if (hasOrdersBetweenDates(prevWeekStart, prevWeekEnd)) {
                    dailySalesSummaryJobService.generateSalesSummary(prevWeekStart, prevWeekEnd, "WEEKLY");
                    log.info("Generated historical weekly summary for week {} to {}", prevWeekStart, prevWeekEnd);
                }

                // Generate monthly summary for previous month (if orders exist)
                LocalDate prevMonthStart = monthStart.minusMonths(1);
                LocalDate prevMonthEnd = monthStart.minusDays(1);
                if (hasOrdersBetweenDates(prevMonthStart, prevMonthEnd)) {
                    dailySalesSummaryJobService.generateSalesSummary(prevMonthStart, prevMonthEnd, "MONTHLY");
                    log.info("Generated historical monthly summary for month {} to {}", prevMonthStart, prevMonthEnd);
                }
            }

            log.info("Initial summary generation completed successfully");
            
        } catch (Exception e) {
            log.error("Failed to generate initial summaries: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a summary exists for a specific date and report type
     */
    private boolean hasSummaryForDateAndType(LocalDate date, String reportType) {
        return dailySalesSummaryRepository.findByReportDateAndReportType(date, reportType).isPresent();
    }

    /**
     * Check if there are orders for a specific date
     */
    private boolean hasOrdersForDate(LocalDate date) {
        return orderRepository.countByCreatedAtBetween(
            date.atStartOfDay(), 
            date.atTime(23, 59, 59)
        ) > 0;
    }

    /**
     * Check if there are orders between two dates
     */
    private boolean hasOrdersBetweenDates(LocalDate startDate, LocalDate endDate) {
        return orderRepository.countByCreatedAtBetween(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        ) > 0;
    }
}
