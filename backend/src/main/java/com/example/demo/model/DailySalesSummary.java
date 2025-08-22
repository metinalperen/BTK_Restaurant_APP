package com.example.demo.model;

import com.example.demo.enums.ItemCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "daily_sales_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_daily_sales_summary_date_type",
                columnNames = {"report_date", "report_type"}
        ))
public class DailySalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "report_date",  nullable = false)
    private LocalDate reportDate;

    @Column(name = "report_type",  nullable = false)
    private String reportType; // DAILY, WEEKLY, MONTHLY

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "average_order_value")
    private BigDecimal averageOrderValue;

    @Column(name = "total_customers")
    private Integer totalCustomers;

    @ManyToOne
    @JoinColumn(name = "most_popular_item_id")
    private Product mostPopularItem;

    @ManyToOne
    @JoinColumn(name = "least_popular_item_id")
    private Product leastPopularItem;

    @Column(name = "total_reservations")
    private Integer totalReservations;


    @Column(name = "sales_by_category", columnDefinition = "TEXT")
    private String salesByCategoryJson; // JSON string for sales by category

    @Column(name = "employee_performance", columnDefinition = "TEXT")
    private String employeePerformanceJson; // JSON string for employee performance data

    // New fields for enhanced analytics
    @Column(name = "top_products_json", columnDefinition = "TEXT")
    private String topProductsJson; // JSON string for top products data

    @Column(name = "top_products_count")
    private Integer topProductsCount; // Number of top products stored

    @Column(name = "period_start_date")
    private LocalDate periodStartDate; // Start date of the reporting period

    @Column(name = "period_end_date")
    private LocalDate periodEndDate; // End date of the reporting period

    // Constructors
    public DailySalesSummary() {
    }

    public DailySalesSummary(LocalDate reportDate, BigDecimal totalRevenue, Integer totalOrders,
                             BigDecimal averageOrderValue, Integer totalCustomers, Product mostPopularItem,
                             Product leastPopularItem, Integer totalReservations, String reportType,
                             String employeePerformanceJson) {
        this.reportDate = reportDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.averageOrderValue = averageOrderValue;
        this.totalCustomers = totalCustomers;
        this.mostPopularItem = mostPopularItem;
        this.leastPopularItem = leastPopularItem;
        this.totalReservations = totalReservations;
        this.reportType = reportType;
        this.employeePerformanceJson = employeePerformanceJson;
    }

    // Enhanced constructor with new fields
    public DailySalesSummary(LocalDate reportDate, LocalDate periodStartDate, LocalDate periodEndDate,
                             BigDecimal totalRevenue, Integer totalOrders, BigDecimal averageOrderValue,
                             Integer totalCustomers, Product mostPopularItem, Product leastPopularItem,
                             Integer totalReservations, String reportType, String employeePerformanceJson,
                             String topProductsJson, Integer topProductsCount) {
        this.reportDate = reportDate;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.averageOrderValue = averageOrderValue;
        this.totalCustomers = totalCustomers;
        this.mostPopularItem = mostPopularItem;
        this.leastPopularItem = leastPopularItem;
        this.totalReservations = totalReservations;
        this.reportType = reportType;
        this.employeePerformanceJson = employeePerformanceJson;
        this.topProductsJson = topProductsJson;
        this.topProductsCount = topProductsCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Integer getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Integer totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public Product getMostPopularItem() {
        return mostPopularItem;
    }

    public void setMostPopularItem(Product mostPopularItem) {
        this.mostPopularItem = mostPopularItem;
    }

    public Product getLeastPopularItem() {
        return leastPopularItem;
    }

    public void setLeastPopularItem(Product leastPopularItem) {
        this.leastPopularItem = leastPopularItem;
    }

    public Integer getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(Integer totalReservations) {
        this.totalReservations = totalReservations;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getSalesByCategoryJson() {
        return salesByCategoryJson;
    }

    public void setSalesByCategoryJson(String salesByCategoryJson) {
        this.salesByCategoryJson = salesByCategoryJson;
    }

    public String getEmployeePerformanceJson() {
        return employeePerformanceJson;
    }

    public void setEmployeePerformanceJson(String employeePerformanceJson) {
        this.employeePerformanceJson = employeePerformanceJson;
    }

    // New getters and setters
    public String getTopProductsJson() {
        return topProductsJson;
    }

    public void setTopProductsJson(String topProductsJson) {
        this.topProductsJson = topProductsJson;
    }

    public Integer getTopProductsCount() {
        return topProductsCount;
    }

    public void setTopProductsCount(Integer topProductsCount) {
        this.topProductsCount = topProductsCount;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public LocalDate getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(LocalDate periodEndDate) {
        this.periodEndDate = periodEndDate;
    }
}
