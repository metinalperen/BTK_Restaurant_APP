package com.example.demo.service;

import com.example.demo.dto.response.DailySalesSummaryResponseDTO;
import com.example.demo.model.DailySalesSummary;
import com.example.demo.repository.DailySalesSummaryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailySalesSummaryService {

    private final DailySalesSummaryRepository repository;
    private final ObjectMapper objectMapper;
    private final DailySalesSummaryJobService jobService;

    public DailySalesSummaryService(DailySalesSummaryRepository repository, DailySalesSummaryJobService jobService) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.jobService = jobService;
    }


    public DailySalesSummaryResponseDTO getSummaryByDateAndType(LocalDate date, String reportType) {
        System.out.println("Searching summary for date: " + date + " and type: " + reportType);

        Optional<DailySalesSummary> optional = repository.findByReportDateAndReportType(date, reportType);

        if (optional.isEmpty()) {
            throw new RuntimeException("Summary not found for: " + date + " with type: " + reportType);
        }

        DailySalesSummary summary = optional.get();
        return convertToResponseDTO(summary);
    }

    public List<DailySalesSummaryResponseDTO> getSummariesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DailySalesSummary> summaries = repository.findByReportDateBetweenOrderByReportDateDesc(startDate, endDate);
        return summaries.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    // Sales by category for a given date range
    public Map<String, String> getSalesByCategory(LocalDate startDate, LocalDate endDate) {
        List<DailySalesSummaryResponseDTO> summaries = getSummariesByDateRange(startDate, endDate);

        Map<String, Double> aggregated = new HashMap<>();

        for (DailySalesSummaryResponseDTO dto : summaries) {
            dto.getSalesByCategory().forEach((category, value) -> {
                double val = 0;
                try {
                    val = Double.parseDouble(value);
                } catch (NumberFormatException ignored) {}
                aggregated.merge(category, val, Double::sum);
            });
        }

        // Double değerleri String’e çeviriyoruz
        Map<String, String> result = new HashMap<>();
        aggregated.forEach((k, v) -> result.put(k, String.valueOf(v)));

        return result;
    }

    public List<DailySalesSummaryResponseDTO> getSummariesByReportType(String reportType) {
        List<DailySalesSummary> summaries = repository.findByReportTypeOrderByReportDateDesc(reportType);
        return summaries.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    
 //********testing **********
    // Manual report generation methods for testing
    public DailySalesSummaryResponseDTO generateDailyReport(LocalDate date) {
        try {
            jobService.generateSalesSummary(date, date, "DAILY");
            return getSummaryByDateAndType(date, "DAILY");
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to generate daily report for " + date + ". No data found for this date.", e);
        }
    }

    public DailySalesSummaryResponseDTO generateWeeklyReport(LocalDate endDate) {
        try {
            LocalDate startDate = endDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            jobService.generateSalesSummary(startDate, endDate, "WEEKLY");
            return getSummaryByDateAndType(endDate, "WEEKLY");
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to generate weekly report for week ending " + endDate + ". No data found for this period.", e);
        }
    }

    public DailySalesSummaryResponseDTO generateMonthlyReport(int year, int month) {
        try {

            LocalDate endDate = LocalDate.of(year, month, 1).withDayOfMonth(
                LocalDate.of(year, month, 1).lengthOfMonth()
            );

            LocalDate startDate = endDate.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
            System.out.println("Start date: " + startDate);
            System.out.println("End date: " + endDate);
            jobService.generateSalesSummary(startDate, endDate, "MONTHLY");
            return getSummaryByDateAndType(endDate, "MONTHLY");
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to generate monthly report for " + year + "-" + month + ". No data found for this month.", e);
        }
    }

    public DailySalesSummaryResponseDTO generateReportForDateRange(LocalDate startDate, LocalDate endDate, String reportType) {
        try {
            jobService.generateSalesSummary(startDate, endDate, reportType);
            return getSummaryByDateAndType(endDate, reportType);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to generate " + reportType + " report for period " + startDate + " to " + endDate + ". No data found for this period.", e);
        }
    }

//**********testing end **********
    
    private DailySalesSummaryResponseDTO convertToResponseDTO(DailySalesSummary summary) {
        DailySalesSummaryResponseDTO dto = new DailySalesSummaryResponseDTO();

        dto.setReportDate(summary.getReportDate());
        dto.setTotalRevenue(summary.getTotalRevenue());
        dto.setTotalOrders(summary.getTotalOrders());
        dto.setAverageOrderValue(summary.getAverageOrderValue());
        dto.setTotalCustomers(summary.getTotalCustomers());

        // Most popular item
        String mostPopularItemName = summary.getMostPopularItem() != null ?
                summary.getMostPopularItem().getName() : "N/A";
        dto.setMostPopularItemName(mostPopularItemName);

        // Least popular item
        String leastPopularItemName = summary.getLeastPopularItem() != null ?
                summary.getLeastPopularItem().getName() : "N/A";
        dto.setLeastPopularItemName(leastPopularItemName);

        // Total reservations
        dto.setTotalReservations(summary.getTotalReservations());

        // Report type
        dto.setReportType(summary.getReportType());

        // Sales by category (convert JSON to Map)
        Map<String, String> salesByCategory = toSalesByCategoryMap(summary.getSalesByCategoryJson());
        dto.setSalesByCategory(salesByCategory);

        // Employee performance (convert JSON to Map)
        Map<String, Object> employeePerformance = toEmployeePerformanceMap(summary.getEmployeePerformanceJson());
        dto.setEmployeePerformance(employeePerformance);

        return dto;
    }

    private Map<String, String> toSalesByCategoryMap(String salesByCategoryJson) {
        if (salesByCategoryJson == null || salesByCategoryJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(salesByCategoryJson, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private Map<String, Object> toEmployeePerformanceMap(String employeePerformanceJson) {
        if (employeePerformanceJson == null || employeePerformanceJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(employeePerformanceJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}