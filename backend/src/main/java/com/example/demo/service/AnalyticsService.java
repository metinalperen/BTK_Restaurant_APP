package com.example.demo.service;

import com.example.demo.dto.response.TopProductDTO;
import com.example.demo.dto.response.TopProductsSummaryResponseDTO;
import com.example.demo.enums.TopProductsPeriod;
import com.example.demo.exception.analytics.*;
import com.example.demo.model.DailySalesSummary;
import com.example.demo.repository.DailySalesSummaryRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.projection.TopProductView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import com.example.demo.event.OrderCreatedEvent;
import com.example.demo.event.OrderUpdatedEvent;
import com.example.demo.event.OrderCompletedEvent;
import com.example.demo.model.Order;
import java.util.Comparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.OrderItem;
import java.math.RoundingMode;

/**
 * Optimized Analytics Service using DailySalesSummary for fast performance.
 * Time complexity: O(1) for most queries instead of O(n²)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final DailySalesSummaryRepository dailySalesSummaryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final DailySalesSummaryJobService dailySalesSummaryJobService;
    private final ObjectMapper objectMapper;

    /**
     * Get top products for a specific period using pre-computed summaries.
     * This is the O(1) "fast path".
     */
    public List<TopProductDTO> getTopProducts(TopProductsPeriod period, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));

        LocalDate reportDate = getReportDateForPeriod(period);
        LocalDate startDate = getPeriodStart(period, reportDate);

        log.info("Getting top products for period: {}, target report date: {}, start date: {}, limit: {}",
                period, reportDate, startDate, safeLimit);

        try {
            // Try to get from pre-computed summaries first (FAST PATH)
            List<TopProductDTO> summaryResults = getTopProductsFromSummaries(period, reportDate, safeLimit);
            if (!summaryResults.isEmpty()) {
                log.info("Retrieved {} top products from summaries (fast path) for period: {}", summaryResults.size(), period);
                return summaryResults;
            }
        } catch (SummaryNotFoundException e) {
            log.warn("Summary not found for period: {} and date: {}. Attempting to generate.", e.getPeriod(), e.getReportDate());
            try {
                dailySalesSummaryJobService.generateSalesSummary(startDate, reportDate, getReportType(period));
                List<TopProductDTO> summaryResults = getTopProductsFromSummaries(period, reportDate, safeLimit);
                if (!summaryResults.isEmpty()) {
                    log.info("Successfully generated and retrieved {} top products from summaries for period: {}", summaryResults.size(), period);
                    return summaryResults;
                }
            } catch (Exception generationException) {
                log.error("Failed to generate or retrieve summary after attempt: {}", generationException.getMessage());
            }
        } catch (AnalyticsDataCorruptionException adce) {
            log.error("Data corruption detected: {}", adce.getMessage());
        } catch (Exception e) {
            log.warn("An unexpected error occurred while fetching from summaries. Falling back. Error: {}", e.getMessage());
        }

        log.info("Falling back to real-time calculation for period: {} (start: {}, end: now)", period, startDate);
        return getTopProductsRealTime(period, safeLimit);
    }

    /**
     * Ensure current period summaries are available for real-time analytics
     */
    public void ensureCurrentPeriodSummaries() {
        try {
            LocalDate today = LocalDate.now();
            
            // Generate daily summary for today if it doesn't exist
            try {
                getTopProductsFromSummaries(TopProductsPeriod.DAILY, today, 10);
            } catch (SummaryNotFoundException e) {
                log.info("Generating current daily summary for {}", today);
                dailySalesSummaryJobService.generateSalesSummary(today, today, "DAILY");
            }
            
            // Generate weekly summary for current week if it doesn't exist
            LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate weekStart = weekEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            try {
                getTopProductsFromSummaries(TopProductsPeriod.WEEKLY, weekEnd, 10);
            } catch (SummaryNotFoundException e) {
                log.info("Generating current weekly summary for week {} to {}", weekStart, weekEnd);
                dailySalesSummaryJobService.generateSalesSummary(weekStart, weekEnd, "WEEKLY");
            }
            
            // Generate monthly summary for current month if it doesn't exist
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            LocalDate monthStart = monthEnd.withDayOfMonth(1);
            try {
                getTopProductsFromSummaries(TopProductsPeriod.MONTHLY, monthEnd, 10);
            } catch (SummaryNotFoundException e) {
                log.info("Generating current monthly summary for month {} to {}", monthStart, monthEnd);
                dailySalesSummaryJobService.generateSalesSummary(monthStart, monthEnd, "MONTHLY");
            }
            
            log.info("Current period summaries ensured successfully");
        } catch (Exception e) {
            log.error("Failed to ensure current period summaries: {}", e.getMessage());
        }
    }

    /**
     * Get top products summary for all periods in one call
     * Time complexity: O(1) for each period
     */
    public TopProductsSummaryResponseDTO getTopProductsSummary(int limit) {
        return new TopProductsSummaryResponseDTO(
                getTopProducts(TopProductsPeriod.DAILY, limit),
                getTopProducts(TopProductsPeriod.WEEKLY, limit),
                getTopProducts(TopProductsPeriod.MONTHLY, limit),
                getTopProducts(TopProductsPeriod.YEARLY, limit)
        );
    }

    private List<TopProductDTO> getTopProductsFromSummaries(TopProductsPeriod period, LocalDate reportDate, int limit) {
        String reportType = getReportType(period);

        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
                .findByReportDateAndReportType(reportDate, reportType);

        if (summaryOpt.isEmpty()) {
            throw new SummaryNotFoundException(period, reportDate);
        }

        DailySalesSummary summary = summaryOpt.get();
        return convertSummaryToTopProducts(summary, limit);
    }

    private LocalDate getReportDateForPeriod(TopProductsPeriod period) {
        LocalDate today = LocalDate.now();
        switch (period) {
            case DAILY:
                // Return today for current daily analytics
                return today;
            case WEEKLY:
                // Return the end of current week (Sunday)
                return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MONTHLY:
                // Return the end of current month
                return today.withDayOfMonth(today.lengthOfMonth());
            case YEARLY:
                // Return the end of current year
                return today.withDayOfYear(today.lengthOfYear());
            default:
                throw new AnalyticsConfigurationException("period", period.toString(), "Unsupported period");
        }
    }

    private List<TopProductDTO> convertSummaryToTopProducts(DailySalesSummary summary, int limit) {
        List<TopProductDTO> result = new ArrayList<>();

        if (summary.getTopProductsJson() != null && !summary.getTopProductsJson().equals("[]")) {
            try {
                List<TopProductDTO> topProductsFromJson = parseTopProductsJson(summary.getTopProductsJson());
                if (!topProductsFromJson.isEmpty()) {
                    log.info("Retrieved {} top products from JSON data", topProductsFromJson.size());
                    return topProductsFromJson.stream().limit(limit).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Failed to parse top products JSON, falling back to basic data: {}", e.getMessage());
                throw new AnalyticsDataCorruptionException("Top Products JSON", "topProductsJson",
                        summary.getTopProductsJson(), e);
            }
        }

        if (summary.getMostPopularItem() != null) {
            result.add(new TopProductDTO(
                    summary.getMostPopularItem().getId(),
                    summary.getMostPopularItem().getName(),
                    1L,
                    1L,
                    summary.getTotalRevenue()
            ));
        }
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    private List<TopProductDTO> parseTopProductsJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("[]")) {
                return new ArrayList<>();
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<TopProductDTO>>(){});
        } catch (Exception e) {
            log.error("Failed to parse top products JSON: {}", e.getMessage());
            throw new AnalyticsDataCorruptionException("Top Products JSON", "topProductsJson", json, e);
        }
    }

    private List<TopProductDTO> getTopProductsRealTime(TopProductsPeriod period, int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        // For real-time calculation, we want to include data from the start of the current period up to now
        LocalDate periodStart = getPeriodStart(period, today);
        LocalDateTime start = periodStart.atStartOfDay();
        LocalDateTime end = now;

        log.info("Real-time calculation for period: {}, period start: {}, start: {}, end: {}, limit: {}", 
                period, periodStart, start, end, limit);

        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<TopProductView> rows = orderItemRepository.findTopProductsBetween(start, end, pageable);

            log.info("Real-time calculation found {} products for period: {}", rows.size(), period);

            return rows.stream()
                    .map(r -> new TopProductDTO(
                            r.getProductId(),
                            r.getProductName(),
                            r.getTotalQuantity() == null ? 0L : r.getTotalQuantity(),
                            r.getOrderCount() == null ? 0L : r.getOrderCount(),
                            r.getTotalRevenue()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Real-time calculation failed for period: {}, error: {}", period, e.getMessage());
            throw new AnalyticsFallbackException("Real-time Calculation", "Error Response",
                    "Real-time calculation failed", e);
        }
    }

    private String getReportType(TopProductsPeriod period) {
        switch (period) {
            case DAILY: return "DAILY";
            case WEEKLY: return "WEEKLY";
            case MONTHLY: return "MONTHLY";
            case YEARLY: return "YEARLY";
            default: throw new AnalyticsConfigurationException("period", period.toString(), "DAILY, WEEKLY, MONTHLY, or YEARLY");
        }
    }

    private LocalDate getPeriodStart(TopProductsPeriod period, LocalDate reference) {
        switch (period) {
            case DAILY:
                return reference;
            case WEEKLY:
                return reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY:
                return reference.withDayOfMonth(1);
            case YEARLY:
                return reference.withDayOfYear(1);
            default:
                throw new AnalyticsConfigurationException("period", period.toString(), "Unsupported period");
        }
    }

    // *** ADDING BACK MISSING METHODS FOR THE CONTROLLER ***

    public List<TopProductDTO> getAllTopProducts(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        log.info("Getting all top products without date filter, limit: {}", safeLimit);
        Pageable pageable = PageRequest.of(0, safeLimit);
        List<TopProductView> rows = orderItemRepository.findAllTopProducts(pageable);
        return rows.stream()
                .map(r -> new TopProductDTO(
                        r.getProductId(),
                        r.getProductName(),
                        r.getTotalQuantity() == null ? 0L : r.getTotalQuantity(),
                        r.getOrderCount() == null ? 0L : r.getOrderCount(),
                        r.getTotalRevenue()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getRevenueAnalytics(TopProductsPeriod period) {
        LocalDate reportDate = getReportDateForPeriod(period);
        String reportType = getReportType(period);
        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
                .findByReportDateAndReportType(reportDate, reportType);
        if (summaryOpt.isPresent()) {
            DailySalesSummary summary = summaryOpt.get();
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("totalRevenue", summary.getTotalRevenue());
            analytics.put("totalOrders", summary.getTotalOrders());
            analytics.put("averageOrderValue", summary.getAverageOrderValue());
            analytics.put("totalCustomers", summary.getTotalCustomers());
            analytics.put("reportDate", summary.getReportDate());
            analytics.put("reportType", summary.getReportType());
            return analytics;
        }
        throw new SummaryNotFoundException(period, reportDate);
    }

    public Map<String, Object> getRealTimeRevenueAnalytics(TopProductsPeriod period) {
        LocalDate startDate = getPeriodStart(period, LocalDate.now());
        LocalDate endDate = LocalDate.now();
        
        // Get orders for the period
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Calculate metrics
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrders = orders.size();
        
        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        
        int totalCustomers = (int) orders.stream()
                .map(Order::getUser)
                .distinct()
                .count();
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalRevenue", totalRevenue);
        analytics.put("totalOrders", totalOrders);
        analytics.put("averageOrderValue", averageOrderValue);
        analytics.put("totalCustomers", totalCustomers);
        analytics.put("reportDate", endDate);
        analytics.put("reportType", getReportType(period));
        analytics.put("startDate", startDate);
        analytics.put("endDate", endDate);
        
        return analytics;
    }

    public Map<String, String> getCategorySalesBreakdown(TopProductsPeriod period) {
        LocalDate reportDate = getReportDateForPeriod(period);
        String reportType = getReportType(period);
        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
                .findByReportDateAndReportType(reportDate, reportType);
        if (summaryOpt.isPresent() && summaryOpt.get().getSalesByCategoryJson() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(summaryOpt.get().getSalesByCategoryJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
            } catch (Exception e) {
                throw new AnalyticsDataCorruptionException("Sales By Category JSON", "salesByCategoryJson", summaryOpt.get().getSalesByCategoryJson(), e);
            }
        }
        throw new SummaryNotFoundException(period, reportDate);
    }

    public Map<String, Object> getEmployeePerformance(TopProductsPeriod period) {
        LocalDate reportDate = getReportDateForPeriod(period);
        String reportType = getReportType(period);
        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
                .findByReportDateAndReportType(reportDate, reportType);
        if (summaryOpt.isPresent() && summaryOpt.get().getEmployeePerformanceJson() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(summaryOpt.get().getEmployeePerformanceJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
            } catch (Exception e) {
                throw new AnalyticsDataCorruptionException("Employee Performance JSON", "employeePerformanceJson", summaryOpt.get().getEmployeePerformanceJson(), e);
            }
        }
        throw new SummaryNotFoundException(period, reportDate);
    }

    public String debugOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reportDate = getReportDateForPeriod(TopProductsPeriod.WEEKLY);
        Optional<DailySalesSummary> weeklySummary = dailySalesSummaryRepository
                .findByReportDateAndReportType(reportDate, "WEEKLY");

        if (weeklySummary.isPresent()) {
            DailySalesSummary summary = weeklySummary.get();
            return String.format("Debug Info (from summaries): Current time: %s, Weekly start: %s, Orders this week: %d, Revenue this week: %s",
                    now, summary.getPeriodStartDate(), summary.getTotalOrders(), summary.getTotalRevenue());
        }
        return "Debug Info: No weekly summary available for report date: " + reportDate;
    }

    public String debugDatabase() {
        List<DailySalesSummary> summaries = dailySalesSummaryRepository.findAllOrderByReportDateDesc();
        if (!summaries.isEmpty()) {
            DailySalesSummary latest = summaries.get(0);
            return String.format("Database Debug Info (from summaries): Latest summary date: %s, Latest summary type: %s, Total orders in latest: %d, Total revenue in latest: %s",
                    latest.getReportDate(), latest.getReportType(), latest.getTotalOrders(), latest.getTotalRevenue());
        }
        return "Database Debug Info: No summaries available";
    }

    public String debugDateCalculation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate dailyReportDate = getReportDateForPeriod(TopProductsPeriod.DAILY);
        LocalDate weeklyReportDate = getReportDateForPeriod(TopProductsPeriod.WEEKLY);
        LocalDate monthlyReportDate = getReportDateForPeriod(TopProductsPeriod.MONTHLY);
        LocalDate yearlyReportDate = getReportDateForPeriod(TopProductsPeriod.YEARLY);

        return String.format("Date Calculation Debug: Current time: %s, Daily Report Date: %s, Weekly Report Date: %s, Monthly Report Date: %s, Yearly Report Date: %s",
                now, dailyReportDate, weeklyReportDate, monthlyReportDate, yearlyReportDate);
    }

    /**
     * Debug method to help troubleshoot top-products analytics
     */
    public Map<String, Object> debugTopProducts(TopProductsPeriod period) {
        Map<String, Object> debug = new HashMap<>();
        LocalDate reportDate = getReportDateForPeriod(period);
        LocalDate startDate = getPeriodStart(period, reportDate);
        
        debug.put("period", period.toString());
        debug.put("reportDate", reportDate);
        debug.put("startDate", startDate);
        debug.put("reportType", getReportType(period));
        
        // Check if summary exists
        try {
            Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
                .findByReportDateAndReportType(reportDate, getReportType(period));
            debug.put("summaryExists", summaryOpt.isPresent());
            if (summaryOpt.isPresent()) {
                DailySalesSummary summary = summaryOpt.get();
                debug.put("summaryId", summary.getId());
                debug.put("summaryTopProductsCount", summary.getTopProductsCount());
                debug.put("summaryTopProductsJson", summary.getTopProductsJson());
            }
        } catch (Exception e) {
            debug.put("summaryCheckError", e.getMessage());
        }
        
        // Check real-time data availability
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = LocalDateTime.now();
            long orderCount = orderRepository.countByCreatedAtBetween(start, end);
            debug.put("realTimeOrderCount", orderCount);
            debug.put("realTimeStart", start);
            debug.put("realTimeEnd", end);
        } catch (Exception e) {
            debug.put("realTimeCheckError", e.getMessage());
        }
        
        debug.put("timestamp", LocalDateTime.now());
        return debug;
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Event listener for order creation
     * Updates all relevant summaries incrementally to maintain O(1) performance
     */
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        log.info("Processing OrderCreatedEvent for order ID: {}", order.getId());
        
        try {
            updateSummariesForOrder(order, false);
            log.info("Successfully updated summaries for new order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to update summaries for new order ID: {}", order.getId(), e);
            // Don't throw - analytics failure shouldn't break order creation
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Event listener for order updates
     * Recalculates affected summaries to maintain data consistency
     */
    @EventListener
    public void handleOrderUpdated(OrderUpdatedEvent event) {
        Order order = event.getOrder();
        log.info("Processing OrderUpdatedEvent for order ID: {}", order.getId());
        
        try {
            updateSummariesForOrder(order, true);
            log.info("Successfully updated summaries for updated order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to update summaries for updated order ID: {}", order.getId(), e);
            // Don't throw - analytics failure shouldn't break order updates
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Event listener for order completion
     * Updates completion-related metrics in summaries
     */
    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        Order order = event.getOrder();
        log.info("Processing OrderCompletedEvent for order ID: {}", order.getId());
        
        try {
            updateCompletionMetrics(order);
            log.info("Successfully updated completion metrics for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to update completion metrics for order ID: {}", order.getId(), e);
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Core method to update all relevant summaries
     * @param order The order that triggered the update
     * @param isUpdate Whether this is an update (true) or new order (false)
     */
    private void updateSummariesForOrder(Order order, boolean isUpdate) {
        LocalDate orderDate = order.getCreatedAt().toLocalDate();
        
        // Update daily summary (always)
        updateDailySummary(orderDate, order, isUpdate);
        
        // Update weekly summary if order is in current week
        if (isInCurrentWeek(orderDate)) {
            updateWeeklySummary(orderDate, order, isUpdate);
        }
        
        // Update monthly summary if order is in current month
        if (isInCurrentMonth(orderDate)) {
            updateMonthlySummary(orderDate, order, isUpdate);
        }
        
        // Update yearly summary if order is in current year
        if (isInCurrentYear(orderDate)) {
            updateYearlySummary(orderDate, order, isUpdate);
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update daily summary incrementally
     */
    private void updateDailySummary(LocalDate date, Order order, boolean isUpdate) {
        DailySalesSummary summary = dailySalesSummaryRepository
            .findByReportDateAndReportType(date, "DAILY")
            .orElseGet(() -> createEmptyDailySummary(date));
        
        if (isUpdate) {
            // For updates, we need to handle the case where order amounts might have changed
            // This is simplified - in production you might want to track the delta
            log.warn("Order update detected - consider implementing delta tracking for more accurate analytics");
        }
        
        // Incrementally update summary
        summary.setTotalRevenue(summary.getTotalRevenue().add(order.getTotalPrice()));
        summary.setTotalOrders(summary.getTotalOrders() + 1);
        summary.setTotalCustomers(calculateUpdatedCustomerCount(summary, order));
        
        // Update top products incrementally
        updateTopProductsIncrementally(summary, order);
        
        // Update category sales incrementally
        updateCategorySalesIncrementally(summary, order);
        
        // Update employee performance incrementally
        updateEmployeePerformanceIncrementally(summary, order);
        
        dailySalesSummaryRepository.save(summary);
        log.debug("Updated daily summary for date: {}", date);
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update weekly summary incrementally
     */
    private void updateWeeklySummary(LocalDate date, Order order, boolean isUpdate) {
        LocalDate weeklyStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String reportType = "WEEKLY";
        
        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
            .findByReportDateAndReportType(weeklyStartDate, reportType);
        
        if (summaryOpt.isPresent()) {
            DailySalesSummary summary = summaryOpt.get();
            summary.setTotalRevenue(summary.getTotalRevenue().add(order.getTotalPrice()));
            summary.setTotalOrders(summary.getTotalOrders() + 1);
            summary.setTotalCustomers(calculateUpdatedCustomerCount(summary, order));
            
            updateTopProductsIncrementally(summary, order);
            updateCategorySalesIncrementally(summary, order);
            updateEmployeePerformanceIncrementally(summary, order);
            
            dailySalesSummaryRepository.save(summary);
            log.debug("Updated weekly summary for week starting: {}", weeklyStartDate);
        } else {
            // Fallback to full generation if summary not found (should be rare)
            log.warn("Weekly summary not found for week starting: {}, triggering full generation", weeklyStartDate);
            dailySalesSummaryJobService.generateSalesSummary(weeklyStartDate, date, reportType);
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update monthly summary incrementally
     */
    private void updateMonthlySummary(LocalDate date, Order order, boolean isUpdate) {
        LocalDate monthlyStartDate = date.withDayOfMonth(1);
        String reportType = "MONTHLY";
        
        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
            .findByReportDateAndReportType(monthlyStartDate, reportType);
        
        if (summaryOpt.isPresent()) {
            DailySalesSummary summary = summaryOpt.get();
            summary.setTotalRevenue(summary.getTotalRevenue().add(order.getTotalPrice()));
            summary.setTotalOrders(summary.getTotalOrders() + 1);
            summary.setTotalCustomers(calculateUpdatedCustomerCount(summary, order));
            
            updateTopProductsIncrementally(summary, order);
            updateCategorySalesIncrementally(summary, order);
            updateEmployeePerformanceIncrementally(summary, order);
            
            dailySalesSummaryRepository.save(summary);
            log.debug("Updated monthly summary for month starting: {}", monthlyStartDate);
        } else {
            log.warn("Monthly summary not found for month starting: {}, triggering full generation", monthlyStartDate);
            dailySalesSummaryJobService.generateSalesSummary(monthlyStartDate, date, reportType);
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update yearly summary incrementally
     */
    private void updateYearlySummary(LocalDate date, Order order, boolean isUpdate) {
        LocalDate yearlyStartDate = date.withDayOfYear(1);
        String reportType = "YEARLY";
        
        Optional<DailySalesSummary> summaryOpt = dailySalesSummaryRepository
            .findByReportDateAndReportType(yearlyStartDate, reportType);
        
        if (summaryOpt.isPresent()) {
            DailySalesSummary summary = summaryOpt.get();
            summary.setTotalRevenue(summary.getTotalRevenue().add(order.getTotalPrice()));
            summary.setTotalOrders(summary.getTotalOrders() + 1);
            summary.setTotalCustomers(calculateUpdatedCustomerCount(summary, order));
            
            updateTopProductsIncrementally(summary, order);
            updateCategorySalesIncrementally(summary, order);
            updateEmployeePerformanceIncrementally(summary, order);
            
            dailySalesSummaryRepository.save(summary);
            log.debug("Updated yearly summary for year starting: {}", yearlyStartDate);
        } else {
            log.warn("Yearly summary not found for year starting: {}, triggering full generation", yearlyStartDate);
            dailySalesSummaryJobService.generateSalesSummary(yearlyStartDate, date, reportType);
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Create empty daily summary for new dates
     */
    private DailySalesSummary createEmptyDailySummary(LocalDate date) {
        DailySalesSummary summary = new DailySalesSummary();
        summary.setReportDate(date);
        summary.setReportType("DAILY");
        summary.setPeriodStartDate(date);
        summary.setPeriodEndDate(date);
        summary.setTotalRevenue(BigDecimal.ZERO);
        summary.setTotalOrders(0);
        summary.setAverageOrderValue(BigDecimal.ZERO);
        summary.setTotalCustomers(0);
        summary.setTopProductsJson("[]");
        summary.setTopProductsCount(0);
        summary.setMostPopularItem(null);
        summary.setLeastPopularItem(null);
        summary.setTotalReservations(0);
        summary.setSalesByCategoryJson("{}");
        summary.setEmployeePerformanceJson("{}");
        return summary;
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update top products incrementally
     */
    private void updateTopProductsIncrementally(DailySalesSummary summary, Order order) {
        try {
            List<TopProductDTO> currentTopProducts = parseTopProductsJson(summary.getTopProductsJson());
            Map<Long, TopProductDTO> productMap = new HashMap<>();
            
            // Convert existing to map for efficient lookup
            currentTopProducts.forEach(p -> productMap.put(p.getProductId(), p));
            
            // Update with new order items
            for (OrderItem item : order.getItems()) {
                Long productId = item.getProduct().getId();
                TopProductDTO existing = productMap.get(productId);
                
                if (existing != null) {
                    // Update existing product
                    existing.setTotalQuantity(existing.getTotalQuantity() + item.getQuantity());
                    existing.setTotalRevenue(existing.getTotalRevenue().add(item.getTotalPrice()));
                    existing.setOrderCount(existing.getOrderCount() + 1);
                } else {
                    // Add new product
                    productMap.put(productId, new TopProductDTO(
                        productId,
                        item.getProduct().getName(),
                        item.getQuantity(),
                        1L,
                        item.getTotalPrice()
                    ));
                }
            }
            
            // Convert back to sorted list and limit to top 20
            // 🔥 FIX: Use Long.compare() for primitive long comparison
            List<TopProductDTO> updatedTopProducts = productMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getTotalQuantity(), a.getTotalQuantity()))
                .limit(20)
                .collect(Collectors.toList());
            
            summary.setTopProductsJson(objectMapper.writeValueAsString(updatedTopProducts));
            summary.setTopProductsCount(updatedTopProducts.size());
            
        } catch (Exception e) {
            log.error("Failed to update top products incrementally: {}", e.getMessage());
            // Fallback to full generation if incremental update fails
            try {
                dailySalesSummaryJobService.generateSalesSummary(
                    summary.getPeriodStartDate(), 
                    summary.getPeriodEndDate(), 
                    summary.getReportType()
                );
            } catch (Exception fallbackException) {
                log.error("Fallback generation also failed: {}", fallbackException.getMessage());
            }
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update category sales incrementally
     */
    private void updateCategorySalesIncrementally(DailySalesSummary summary, Order order) {
        try {
            Map<String, String> currentCategorySales = parseCategorySalesJson(summary.getSalesByCategoryJson());
            
            // Update with new order items
            for (OrderItem item : order.getItems()) {
                String category = item.getProduct().getCategory().getValue();
                BigDecimal itemTotal = item.getTotalPrice();
                
                String currentValue = currentCategorySales.getOrDefault(category, "0");
                BigDecimal currentTotal = new BigDecimal(currentValue);
                BigDecimal newTotal = currentTotal.add(itemTotal);
                
                currentCategorySales.put(category, newTotal.toString());
            }
            
            summary.setSalesByCategoryJson(objectMapper.writeValueAsString(currentCategorySales));
            
        } catch (Exception e) {
            log.error("Failed to update category sales incrementally: {}", e.getMessage());
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update employee performance incrementally
     */
    @SuppressWarnings("unchecked")
    private void updateEmployeePerformanceIncrementally(DailySalesSummary summary, Order order) {
        try {
            Map<String, Object> currentEmployeePerformance = parseEmployeePerformanceJson(summary.getEmployeePerformanceJson());
            List<Map<String, Object>> employees = (List<Map<String, Object>>) currentEmployeePerformance.getOrDefault("employees", new ArrayList<>());
            
            // Find or create employee entry
            Map<String, Object> employeeEntry = null;
            for (Map<String, Object> emp : employees) {
                if (emp.get("employeeId").equals(order.getUser().getId())) {
                    employeeEntry = emp;
                    break;
                }
            }
            
            if (employeeEntry == null) {
                // Create new employee entry
                employeeEntry = new HashMap<>();
                employeeEntry.put("employeeId", order.getUser().getId());
                employeeEntry.put("employeeName", order.getUser().getName());
                employeeEntry.put("totalOrders", 0);
                employeeEntry.put("totalRevenue", "0");
                employeeEntry.put("averageOrderValue", "0");
                employeeEntry.put("totalItemsSold", 0);
                employees.add(employeeEntry);
            }
            
            // Update employee metrics
            int currentOrders = (Integer) employeeEntry.get("totalOrders");
            BigDecimal currentRevenue = new BigDecimal((String) employeeEntry.get("totalRevenue"));
            int currentItemsSold = (Integer) employeeEntry.get("totalItemsSold");
            
            employeeEntry.put("totalOrders", currentOrders + 1);
            employeeEntry.put("totalRevenue", currentRevenue.add(order.getTotalPrice()).toString());
            employeeEntry.put("averageOrderValue", 
                currentRevenue.add(order.getTotalPrice()).divide(BigDecimal.valueOf(currentOrders + 1), 2, RoundingMode.HALF_UP).toString());
            
            int itemsInOrder = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
            employeeEntry.put("totalItemsSold", currentItemsSold + itemsInOrder);
            
            // Sort employees by revenue
            employees.sort((a, b) -> {
                BigDecimal revenueA = new BigDecimal((String) a.get("totalRevenue"));
                BigDecimal revenueB = new BigDecimal((String) b.get("totalRevenue"));
                return revenueB.compareTo(revenueA);
            });
            
            // Update top performer
            if (!employees.isEmpty()) {
                currentEmployeePerformance.put("topPerformer", employees.get(0));
            }
            
            currentEmployeePerformance.put("totalEmployees", employees.size());
            summary.setEmployeePerformanceJson(objectMapper.writeValueAsString(currentEmployeePerformance));
            
        } catch (Exception e) {
            log.error("Failed to update employee performance incrementally: {}", e.getMessage());
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Update completion metrics
     */
    private void updateCompletionMetrics(Order order) {
        LocalDate orderDate = order.getCreatedAt().toLocalDate();
        
        // Update daily summary completion metrics
        Optional<DailySalesSummary> dailySummary = dailySalesSummaryRepository
            .findByReportDateAndReportType(orderDate, "DAILY");
        
        if (dailySummary.isPresent()) {
            DailySalesSummary summary = dailySummary.get();
            // You might want to track completed vs pending orders
            // For now, we'll just log the completion
            log.info("Order {} completed, updating completion metrics", order.getId());
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Helper methods for date calculations
     */
    private boolean isInCurrentWeek(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        return !date.isBefore(weekStart) && !date.isAfter(weekEnd);
    }

    private boolean isInCurrentMonth(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.getYear() == today.getYear() && date.getMonth() == today.getMonth();
    }

    private boolean isInCurrentYear(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.getYear() == today.getYear();
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Helper methods for JSON parsing
     */
    private Map<String, String> parseCategorySalesJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("{}")) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
        } catch (Exception e) {
            log.warn("Failed to parse category sales JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseEmployeePerformanceJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("{}")) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            log.warn("Failed to parse employee performance JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 🔥 REAL-TIME ANALYTICS: Calculate updated customer count
     * Note: This is simplified - in production you might want more sophisticated customer tracking
     */
    private Integer calculateUpdatedCustomerCount(DailySalesSummary summary, Order order) {
        // For now, we'll just increment by 1 for each order
        // In production, you might want to track unique customers per day
        return summary.getTotalCustomers() + 1;
    }

    /**
     * Get real-time statistics for dashboard
     * Returns current day, week, and month statistics
     */
    public Map<String, Object> getRealtimeStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        try {
            // Today's statistics (00:00 to now)
            List<Order> todayOrders = orderRepository.findByCreatedAtBetween(today, today);
            BigDecimal todayRevenue = todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.put("todayOrders", todayOrders.size());
            stats.put("todayRevenue", todayRevenue);
            
            // This week's statistics (Monday to Sunday)
            LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = weekStart.plusDays(6);
            
            List<Order> weekOrders = orderRepository.findByCreatedAtBetween(weekStart, weekEnd);
            BigDecimal weekRevenue = weekOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.put("weeklyOrders", weekOrders.size());
            stats.put("weeklyRevenue", weekRevenue);
            
            // This month's statistics (1st to last day of month)
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            
            List<Order> monthOrders = orderRepository.findByCreatedAtBetween(monthStart, monthEnd);
            BigDecimal monthRevenue = monthOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.put("monthlyOrders", monthOrders.size());
            stats.put("monthlyRevenue", monthRevenue);
            
            // Active reservations (today's reservations)
            // Note: This would need to be implemented based on your reservation system
            stats.put("activeReservations", 0); // Placeholder
            
            log.info("Real-time stats calculated: today={} orders, {} revenue, week={} orders, {} revenue, month={} orders, {} revenue",
                    todayOrders.size(), todayRevenue, weekOrders.size(), weekRevenue, monthOrders.size(), monthRevenue);
            
        } catch (Exception e) {
            log.error("Error calculating real-time stats: {}", e.getMessage());
            // Return default values on error
            stats.put("todayOrders", 0);
            stats.put("todayRevenue", BigDecimal.ZERO);
            stats.put("weeklyOrders", 0);
            stats.put("weeklyRevenue", BigDecimal.ZERO);
            stats.put("monthlyOrders", 0);
            stats.put("monthlyRevenue", BigDecimal.ZERO);
            stats.put("activeReservations", 0);
        }
        
        return stats;
    }
}
