package com.example.demo.controller;

import com.example.demo.dto.response.DailySalesSummaryResponseDTO;
import com.example.demo.service.DailySalesSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Sales Summary Reports",
        description = "API for retrieving daily, weekly, and monthly sales summaries with comprehensive analytics."
)
@RestController
@RequestMapping("/api/daily-sales-summary")
@CrossOrigin(origins = "*")
public class DailySalesSummaryController {

    private final DailySalesSummaryService service;

    public DailySalesSummaryController(DailySalesSummaryService service) {
        this.service = service;
    }

    @GetMapping("/daily/{date}")
    @Operation(
            summary = "Get daily sales summary for specific date",
            description = "Retrieves a comprehensive daily sales summary for a specific date including revenue, orders, customers, products, categories, reservations, and employee performance."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> getDailySummary(
            @Parameter(description = "Date for the summary (format: yyyy-MM-dd)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
            ) {
        try {
            DailySalesSummaryResponseDTO response = service.getSummaryByDateAndType(date, "DAILY");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/daily")
    @Operation(
            summary = "Get all daily sales summaries",
            description = "Retrieves all daily sales summaries in the database, ordered by date (newest first)."
    )
    public ResponseEntity<List<DailySalesSummaryResponseDTO>> getAllDailySummaries() {
        try {
            List<DailySalesSummaryResponseDTO> responses = service.getSummariesByReportType("DAILY");
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/weekly/{endDate}")
    @Operation(
            summary = "Get weekly sales summary for specific week",
            description = "Retrieves a comprehensive weekly sales summary from Monday to the specified end date."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> getWeeklySummary(
            @Parameter(description = "End date for the week (format: yyyy-MM-dd)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
            ) {
        try {
            DailySalesSummaryResponseDTO response = service.getSummaryByDateAndType(endDate, "WEEKLY");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/weekly")
    @Operation(
            summary = "Get all weekly sales summaries",
            description = "Retrieves all weekly sales summaries in the database, ordered by date (newest first)."
    )
    public ResponseEntity<List<DailySalesSummaryResponseDTO>> getAllWeeklySummaries() {
        try {
            List<DailySalesSummaryResponseDTO> responses = service.getSummariesByReportType("WEEKLY");
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/monthly/{year}/{month}")
    @Operation(
            summary = "Get monthly sales summary for specific month",
            description = "Retrieves a comprehensive monthly sales summary for the specified year and month."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> getMonthlySummary(
            @Parameter(description = "Year (e.g., 2023)", required = true)
            @PathVariable int year,
            @Parameter(description = "Month (1-12)", required = true)
            @PathVariable int month
    ) {
        try {
            LocalDate endDate = LocalDate.of(year, month, 1).withDayOfMonth(
                LocalDate.of(year, month, 1).lengthOfMonth()
            );
            DailySalesSummaryResponseDTO response = service.getSummaryByDateAndType(endDate, "MONTHLY");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/monthly")
    @Operation(
            summary = "Get all monthly sales summaries",
            description = "Retrieves all monthly sales summaries in the database, ordered by date (newest first)."
    )
    public ResponseEntity<List<DailySalesSummaryResponseDTO>> getAllMonthlySummaries() {
        try {
            List<DailySalesSummaryResponseDTO> responses = service.getSummariesByReportType("MONTHLY");
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/sales-by-category")
    @Operation(
            summary = "Get sales by category",
            description = "Retrieves sales totals grouped by category for a specific date or date range."
    )
    public ResponseEntity<Map<String, String>> getSalesByCategory(
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd). Optional; if not provided, single day report is returned.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            LocalDate finalEndDate = endDate != null ? endDate : startDate;
            Map<String, String> salesByCategory = service.getSalesByCategory(startDate, finalEndDate);
            return ResponseEntity.ok(salesByCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/date-range")
    @Operation(
            summary = "Get sales summaries by date range",
            description = "Retrieves sales summaries for a specified date range."
    )
    public ResponseEntity<List<DailySalesSummaryResponseDTO>> getSummariesByDateRange(
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<DailySalesSummaryResponseDTO> responses = service.getSummariesByDateRange(startDate, endDate);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== MANUAL REPORT GENERATION ENDPOINTS (FOR TESTING) ==========
    
    @PostMapping("/generate/daily/{date}")
    @Operation(
            summary = "Generate daily sales summary for specific date",
            description = "Manually generates a daily sales summary for the specified date. Use this for testing when no reports exist."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> generateDailyReport(
            @Parameter(description = "Date for the summary (format: yyyy-MM-dd)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailySalesSummaryResponseDTO response = service.generateDailyReport(date);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate/weekly/{endDate}")
    @Operation(
            summary = "Generate weekly sales summary for specific week",
            description = "Manually generates a weekly sales summary from Monday to the specified end date. Use this for testing."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> generateWeeklyReport(
            @Parameter(description = "End date for the week (format: yyyy-MM-dd)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            DailySalesSummaryResponseDTO response = service.generateWeeklyReport(endDate);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate/monthly/{year}/{month}")
    @Operation(
            summary = "Generate monthly sales summary for specific month",
            description = "Manually generates a monthly sales summary for the specified year and month. Use this for testing."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> generateMonthlyReport(
            @Parameter(description = "Year (e.g., 2023)", required = true)
            @PathVariable int year,
            @Parameter(description = "Month (1-12)", required = true)
            @PathVariable int month) {
        try {
            DailySalesSummaryResponseDTO response = service.generateMonthlyReport(year, month);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate/date-range")
    @Operation(
            summary = "Generate sales summary for date range",
            description = "Manually generates a sales summary for the specified date range and report type. Use this for testing."
    )
    public ResponseEntity<DailySalesSummaryResponseDTO> generateReportForDateRange(
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Report type (DAILY, WEEKLY, MONTHLY)", required = true)
            @RequestParam String reportType) {
        try {
            DailySalesSummaryResponseDTO response = service.generateReportForDateRange(startDate, endDate, reportType);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}