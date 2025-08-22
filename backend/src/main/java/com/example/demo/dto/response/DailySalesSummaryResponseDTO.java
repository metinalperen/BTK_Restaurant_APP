package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class DailySalesSummaryResponseDTO {

    @Schema(description = "Report date in ISO format (yyyy-MM-dd)", example = "2023-10-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "Total revenue (varies by report type: daily/weekly/monthly)", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalRevenue;

    @Schema(description = "Total number of orders (varies by report type: daily/weekly/monthly)", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalOrders;

    @Schema(description = "Average order value (varies by report type: daily/weekly/monthly)", example = "15.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal averageOrderValue;

    @Schema(description = "Total number of unique customers (varies by report type: daily/weekly/monthly)", example = "80", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCustomers;

    @Schema(description = "Name of the most popular item sold (varies by report type: daily/weekly/monthly)", example = "Espresso", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mostPopularItemName;

    @Schema(description = "Name of the least popular item sold (varies by report type: daily/weekly/monthly)", example = "Decaf Coffee", requiredMode = Schema.RequiredMode.REQUIRED)
    private String leastPopularItemName;

    @Schema(description = "Total number of reservations (varies by report type: daily/weekly/monthly)", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalReservations;

    @Schema(description = "Type of report (DAILY, WEEKLY, MONTHLY)", example = "DAILY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reportType;

    @Schema(description = "Sales breakdown by category", example = "{\"drinks\": \"500.00\", \"main_dishes\": \"800.00\"}", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> salesByCategory;

    @Schema(description = "Employee performance data", example = "{\"employees\": [...], \"topPerformer\": {...}}", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> employeePerformance;

    // Constructors
    public DailySalesSummaryResponseDTO() {
    }

    public DailySalesSummaryResponseDTO(LocalDate reportDate, BigDecimal totalRevenue, Integer totalOrders,
                                        BigDecimal averageOrderValue, Integer totalCustomers, String mostPopularItemName,
                                        String leastPopularItemName, Integer totalReservations, String reportType,
                                        Map<String, String> salesByCategory, Map<String, Object> employeePerformance) {
        this.reportDate = reportDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.averageOrderValue = averageOrderValue;
        this.totalCustomers = totalCustomers;
        this.mostPopularItemName = mostPopularItemName;
        this.leastPopularItemName = leastPopularItemName;
        this.totalReservations = totalReservations;
        this.reportType = reportType;
        this.salesByCategory = salesByCategory;
        this.employeePerformance = employeePerformance;
    }

    // Getters and Setters
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

    public String getMostPopularItemName() {
        return mostPopularItemName;
    }

    public void setMostPopularItemName(String mostPopularItemName) {
        this.mostPopularItemName = mostPopularItemName;
    }

    public String getLeastPopularItemName() {
        return leastPopularItemName;
    }

    public void setLeastPopularItemName(String leastPopularItemName) {
        this.leastPopularItemName = leastPopularItemName;
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

    public Map<String, String> getSalesByCategory() {
        return salesByCategory;
    }

    public void setSalesByCategory(Map<String, String> salesByCategory) {
        this.salesByCategory = salesByCategory;
    }

    public Map<String, Object> getEmployeePerformance() {
        return employeePerformance;
    }

    public void setEmployeePerformance(Map<String, Object> employeePerformance) {
        this.employeePerformance = employeePerformance;
    }
}