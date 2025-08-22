package com.example.demo.service;

import com.example.demo.exception.analytics.*;
import com.example.demo.enums.ItemCategory;
import com.example.demo.model.DailySalesSummary;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.DailySalesSummaryRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReservationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailySalesSummaryJobService {

    private final OrderRepository orderRepository;
    private final DailySalesSummaryRepository dailySalesSummaryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper;

    public DailySalesSummaryJobService(OrderRepository orderRepository,
                                       DailySalesSummaryRepository dailySalesSummaryRepository,
                                       OrderItemRepository orderItemRepository,
                                       ReservationRepository reservationRepository) {
        this.orderRepository = orderRepository;
        this.dailySalesSummaryRepository = dailySalesSummaryRepository;
        this.orderItemRepository = orderItemRepository;
        this.reservationRepository = reservationRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?") // Fire at 1 AM every day
    public void generateDailySalesSummary() {
        LocalDate date = LocalDate.now().minusDays(1);
        generateSalesSummary(date, date, "DAILY");
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * MON") // Fire at 2 AM every Monday
    public void generateWeeklySalesSummary() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        generateSalesSummary(startDate, endDate, "WEEKLY");
    }

    @Transactional
    @Scheduled(cron = "0 0 3 1 * ?") // Fire at 3 AM on the 1st of every month
    public void generateMonthlySalesSummary() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.with(TemporalAdjusters.firstDayOfMonth());
        generateSalesSummary(startDate, endDate, "MONTHLY");
    }

    //**********testing **********
    //private void generateSalesSummary(LocalDate startDate, LocalDate endDate, String reportType)
    public void generateSalesSummary(LocalDate startDate, LocalDate endDate, String reportType) {
        System.out.println("Generating " + reportType + " report for period: " + startDate + " to " + endDate);
        
        // Check for resource exhaustion
        checkResourceAvailability();
        
        // Check if a summary already exists for this date and report type
        Optional<DailySalesSummary> existingSummary = dailySalesSummaryRepository.findByReportDateAndReportType(endDate, reportType);
        if (existingSummary.isPresent()) {
            System.out.println("Updating existing " + reportType + " summary for date: " + endDate);
            // Update existing summary
            DailySalesSummary summary = existingSummary.get();
            try {
                updateSummaryData(summary, startDate, endDate, reportType);
                dailySalesSummaryRepository.save(summary);
            } catch (Exception e) {
                throw new AnalyticsGenerationException("Failed to update existing summary", null, startDate, endDate, reportType, e);
            }
            return;
        }

        System.out.println("Creating new " + reportType + " summary for date: " + endDate);
        // Create new summary
        try {
            DailySalesSummary newSummary = createNewSummary(startDate, endDate, reportType);
            dailySalesSummaryRepository.save(newSummary);
            System.out.println("Successfully created " + reportType + " summary for date: " + endDate);
        } catch (Exception e) {
            throw new AnalyticsGenerationException("Failed to create new summary", null, startDate, endDate, reportType, e);
        }
    }

    /**
     * Check if system has sufficient resources for summary generation
     */
    private void checkResourceAvailability() {
        // Check memory usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        // If using more than 80% of available memory, throw resource exhaustion exception
        if (usedMemory > (maxMemory * 0.8)) {
            throw new AnalyticsResourceExhaustedException("Memory", usedMemory, maxMemory, "Summary Generation");
        }
        
        // Check available disk space (simplified check)
        // In production, implement proper disk space checking
    }

    private void updateSummaryData(DailySalesSummary summary, LocalDate startDate, LocalDate endDate, String reportType) {
        // Set timeout for summary generation
        long startTime = System.currentTimeMillis();
        Duration timeout = Duration.ofMinutes(5); // 5 minutes timeout
        
        try {
            List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);

            // Initialize default values
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalOrders = 0;
            BigDecimal averageOrderValue = BigDecimal.ZERO;
            int totalCustomers = 0;
            Product mostPopularItem = null;
            Product leastPopularItem = null;
            Map<ItemCategory, BigDecimal> salesByCategory = new HashMap<>();
            int totalReservations = 0;
            Map<String, Object> employeePerformance = new HashMap<>();
            String topProductsJson = "{}";
            int topProductsCount = 0;

            if (!orders.isEmpty()) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > timeout.toMillis()) {
                    throw new SummaryGenerationTimeoutException(timeout, null, startDate, endDate);
                }
                
                // Basic metrics
                totalRevenue = orders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                totalOrders = orders.size();
                averageOrderValue = totalOrders > 0 ?
                        totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                        BigDecimal.ZERO;

                totalCustomers = (int) orders.stream()
                    .map(Order::getUser)
                    .distinct()
                    .count();

                // Product analysis
                mostPopularItem = findMostPopularItem(startDate, endDate);
                leastPopularItem = findLeastPopularItem(startDate, endDate);
                
                // Sales by categories
                salesByCategory = calculateSalesByCategory(startDate, endDate);
                
                // Employee performance analysis
                employeePerformance = calculateEmployeePerformance(startDate, endDate);
                
                // Top products analysis for analytics
                topProductsJson = calculateTopProductsJson(startDate, endDate);
                topProductsCount = calculateTopProductsCount(startDate, endDate);
            }
            
            // Total reservations (can exist even without orders)
            totalReservations = reservationRepository.findByReservationTimeBetween(startDate, LocalTime.MIN, endDate, LocalTime.MAX).size();

            // Convert to JSON
            String salesByCategoryJson = convertSalesByCategoryToJson(salesByCategory);
            String employeePerformanceJson = convertEmployeePerformanceToJson(employeePerformance);

            // Update summary data
            summary.setTotalRevenue(totalRevenue);
            summary.setTotalOrders(totalOrders);
            summary.setAverageOrderValue(averageOrderValue);
            summary.setTotalCustomers(totalCustomers);
            summary.setMostPopularItem(mostPopularItem);
            summary.setLeastPopularItem(leastPopularItem);
            summary.setTotalReservations(totalReservations);
            summary.setSalesByCategoryJson(salesByCategoryJson);
            summary.setEmployeePerformanceJson(employeePerformanceJson);
            summary.setTopProductsJson(topProductsJson);
            summary.setTopProductsCount(topProductsCount);
            summary.setPeriodStartDate(startDate);
            summary.setPeriodEndDate(endDate);
            
        } catch (SummaryGenerationTimeoutException e) {
            throw e; // Re-throw timeout exception
        } catch (Exception e) {
            throw new AnalyticsGenerationException("Failed to update summary data", null, startDate, endDate, reportType, e);
        }
    }

    private DailySalesSummary createNewSummary(LocalDate startDate, LocalDate endDate, String reportType) {
        // Set timeout for summary generation
        long startTime = System.currentTimeMillis();
        Duration timeout = Duration.ofMinutes(5); // 5 minutes timeout
        
        try {
            List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);

            // Initialize default values
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalOrders = 0;
            BigDecimal averageOrderValue = BigDecimal.ZERO;
            int totalCustomers = 0;
            Product mostPopularItem = null;
            Product leastPopularItem = null;
            Map<ItemCategory, BigDecimal> salesByCategory = new HashMap<>();
            int totalReservations = 0;
            Map<String, Object> employeePerformance = new HashMap<>();
            String topProductsJson = "{}";
            int topProductsCount = 0;

            if (!orders.isEmpty()) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > timeout.toMillis()) {
                    throw new SummaryGenerationTimeoutException(timeout, null, startDate, endDate);
                }
                
                // Basic metrics
                totalRevenue = orders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                totalOrders = orders.size();
                averageOrderValue = totalOrders > 0 ?
                        totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                        BigDecimal.ZERO;

                totalCustomers = (int) orders.stream()
                    .map(Order::getUser)
                    .distinct()
                    .count();

                // Product analysis
                mostPopularItem = findMostPopularItem(startDate, endDate);
                leastPopularItem = findLeastPopularItem(startDate, endDate);
                
                // Sales by categories
                salesByCategory = calculateSalesByCategory(startDate, endDate);
                
                // Employee performance analysis
                employeePerformance = calculateEmployeePerformance(startDate, endDate);
                
                // Top products analysis for analytics
                topProductsJson = calculateTopProductsJson(startDate, endDate);
                topProductsCount = calculateTopProductsCount(startDate, endDate);
            }
            
            // Total reservations (can exist even without orders)
            totalReservations = reservationRepository.findByReservationTimeBetween(startDate, LocalTime.MIN, endDate, LocalTime.MAX).size();

            // Convert to JSON
            String salesByCategoryJson = convertSalesByCategoryToJson(salesByCategory);
            String employeePerformanceJson = convertEmployeePerformanceToJson(employeePerformance);

            // Create summary object with enhanced constructor
            DailySalesSummary summary = new DailySalesSummary(
                endDate,
                startDate,
                endDate,
                totalRevenue,
                totalOrders,
                averageOrderValue,
                totalCustomers,
                mostPopularItem,
                leastPopularItem,
                totalReservations,
                reportType,
                employeePerformanceJson,
                topProductsJson,
                topProductsCount
            );
            
            summary.setSalesByCategoryJson(salesByCategoryJson);

            return summary;
            
        } catch (SummaryGenerationTimeoutException e) {
            throw e; // Re-throw timeout exception
        } catch (Exception e) {
            throw new AnalyticsGenerationException("Failed to create new summary", null, startDate, endDate, reportType, e);
        }
    }

    private Product findMostPopularItem(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        List<OrderItem> items = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .toList();

        Map<Product, Integer> productCounts = items.stream()
            .collect(Collectors.groupingBy(OrderItem::getProduct, Collectors.summingInt(OrderItem::getQuantity)));

        return productCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private Product findLeastPopularItem(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        List<OrderItem> items = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .toList();

        Map<Product, Integer> productCounts = items.stream()
            .collect(Collectors.groupingBy(OrderItem::getProduct, Collectors.summingInt(OrderItem::getQuantity)));

        return productCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private Map<ItemCategory, BigDecimal> calculateSalesByCategory(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        Map<ItemCategory, BigDecimal> categorySales = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                ItemCategory category = item.getProduct().getCategory();
                BigDecimal itemTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                categorySales.merge(category, itemTotal, BigDecimal::add);
            }
        }
        
        return categorySales;
    }

    private Map<String, Object> calculateEmployeePerformance(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Debug logging
        System.out.println("=== Employee Performance Calculation Debug ===");
        System.out.println("Date range: " + startDate + " to " + endDate);
        System.out.println("Total orders found: " + orders.size());
        
        Map<String, Object> employeePerformance = new HashMap<>();
        Map<User, List<Order>> ordersByEmployee = orders.stream()
            .collect(Collectors.groupingBy(Order::getUser));
        
        System.out.println("Orders grouped by employee: " + ordersByEmployee.size() + " employees");
        
        List<Map<String, Object>> employeeStats = new ArrayList<>();
        
        for (Map.Entry<User, List<Order>> entry : ordersByEmployee.entrySet()) {
            User employee = entry.getKey();
            List<Order> employeeOrders = entry.getValue();
            
            System.out.println("Processing employee: " + employee.getName() + " (ID: " + employee.getId() + ") with " + employeeOrders.size() + " orders");
            
            // Calculate employee metrics
            BigDecimal totalRevenue = employeeOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int totalOrders = employeeOrders.size();
            BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
            
            // Calculate total items sold by this employee
            int totalItemsSold = employeeOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .mapToInt(OrderItem::getQuantity)
                .sum();
            
            System.out.println("Employee " + employee.getName() + " stats: Revenue=" + totalRevenue + ", Orders=" + totalOrders + ", Avg=" + averageOrderValue + ", Items=" + totalItemsSold);
            
            Map<String, Object> employeeStat = new HashMap<>();
            employeeStat.put("employeeId", employee.getId());
            employeeStat.put("employeeName", employee.getName());
            employeeStat.put("totalOrders", totalOrders);
            employeeStat.put("totalRevenue", totalRevenue.toString());
            employeeStat.put("averageOrderValue", averageOrderValue.toString());
            employeeStat.put("totalItemsSold", totalItemsSold);
            
            employeeStats.add(employeeStat);
        }
        
        // Sort by total revenue (descending)
        employeeStats.sort((a, b) -> {
            BigDecimal revenueA = new BigDecimal((String) a.get("totalRevenue"));
            BigDecimal revenueB = new BigDecimal((String) b.get("totalRevenue"));
            return revenueB.compareTo(revenueA);
        });
        
        employeePerformance.put("employees", employeeStats);
        employeePerformance.put("topPerformer", employeeStats.isEmpty() ? null : employeeStats.get(0));
        employeePerformance.put("totalEmployees", employeeStats.size());
        
        System.out.println("Final employee performance data: " + employeePerformance);
        System.out.println("=== End Employee Performance Calculation Debug ===");
        
        return employeePerformance;
    }

    private String convertSalesByCategoryToJson(Map<ItemCategory, BigDecimal> salesByCategory) {
        try {
            Map<String, String> categorySalesString = salesByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().getValue(),
                    entry -> entry.getValue().toString()
                ));
            return objectMapper.writeValueAsString(categorySalesString);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String convertEmployeePerformanceToJson(Map<String, Object> employeePerformance) {
        try {
            return objectMapper.writeValueAsString(employeePerformance);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * Calculate top products JSON for analytics
     * This stores the top products data directly in the summary for O(1) access
     */
    private String calculateTopProductsJson(LocalDate startDate, LocalDate endDate) {
        try {
            // Get top products using the same logic as AnalyticsService
            List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
            
            if (orders.isEmpty()) {
                return "[]";
            }
            
            // Aggregate order items by product
            Map<Product, ProductSalesData> productSales = new HashMap<>();
            
            for (Order order : orders) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    ProductSalesData salesData = productSales.computeIfAbsent(product, 
                        k -> new ProductSalesData(product.getId(), product.getName()));
                    
                    salesData.addQuantity(item.getQuantity());
                    salesData.addRevenue(item.getTotalPrice());
                    salesData.incrementOrderCount();
                }
            }
            
            // Convert to list and sort by quantity (descending)
            List<ProductSalesData> topProducts = productSales.values().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalQuantity(), a.getTotalQuantity()))
                .limit(20) // Store top 20 products
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsString(topProducts);
            
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Calculate the count of top products stored
     */
    private int calculateTopProductsCount(LocalDate startDate, LocalDate endDate) {
        try {
            List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
            
            if (orders.isEmpty()) {
                return 0;
            }
            
            // Count unique products
            Set<Long> uniqueProducts = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());
            
            return uniqueProducts.size();
            
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Helper class for storing product sales data
     */
    private static class ProductSalesData {
        private Long productId;
        private String productName;
        private int totalQuantity;
        private BigDecimal totalRevenue;
        private int orderCount;
        
        public ProductSalesData(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
            this.totalQuantity = 0;
            this.totalRevenue = BigDecimal.ZERO;
            this.orderCount = 0;
        }
        
        public void addQuantity(int quantity) {
            this.totalQuantity += quantity;
        }
        
        public void addRevenue(BigDecimal revenue) {
            this.totalRevenue = this.totalRevenue.add(revenue);
        }
        
        public void incrementOrderCount() {
            this.orderCount++;
        }
        
        // Getters
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getTotalQuantity() { return totalQuantity; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public int getOrderCount() { return orderCount; }
    }

    // Legacy method for backward compatibility
    private Product getMostPopularItemOnDate(LocalDate date) {
        return findMostPopularItem(date, date);
    }
}

