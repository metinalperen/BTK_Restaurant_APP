package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.response.TopProductDTO;
import com.example.demo.dto.response.TopProductsSummaryResponseDTO;
import com.example.demo.enums.TopProductsPeriod;
import com.example.demo.model.DailySalesSummary;
import com.example.demo.repository.DailySalesSummaryRepository;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.DailySalesSummaryJobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * En çok satan ürünler grafiği için HTTP uçları.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics endpoints for restaurant data")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final DailySalesSummaryJobService dailySalesSummaryJobService;
    private final DailySalesSummaryRepository dailySalesSummaryRepository;

    @GetMapping("/top-products")
    @Operation(
        summary = "Get top-selling products for a period",
        description = "Retrieves the top-selling products for the specified time period (DAILY, WEEKLY, MONTHLY, YEARLY)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top products",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 25, \"totalRevenue\": 1250.0, \"period\": \"DAILY\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TopProductDTO>> getTopProducts(
        @Parameter(description = "Time period for top products", example = "DAILY", required = true)
        @RequestParam(name = "period") TopProductsPeriod period,
        @Parameter(description = "Maximum number of products to return", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopProducts(period, limit));
    }

    @GetMapping("/top-products/summary")
    @Operation(
        summary = "Get daily, weekly, monthly, yearly top-selling products in one call",
        description = "Retrieves top-selling products for all time periods (daily, weekly, monthly, yearly) in a single API call for efficient data retrieval."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top products summary",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductsSummaryResponseDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"daily\": [{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 25, \"totalRevenue\": 1250.0, \"period\": \"DAILY\"}], \"weekly\": [...], \"monthly\": [...], \"yearly\": [...]}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TopProductsSummaryResponseDTO> getTopProductsSummary(
        @Parameter(description = "Maximum number of products to return for each period", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopProductsSummary(limit));
    }

    @GetMapping("/top-products/daily")
    @Operation(
        summary = "Get daily top-selling products",
        description = "Retrieves the top-selling products for the current day."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved daily top products",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 25, \"totalRevenue\": 1250.0, \"period\": \"DAILY\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TopProductDTO>> getDaily(
        @Parameter(description = "Maximum number of products to return", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(TopProductsPeriod.DAILY, limit));
    }

    @GetMapping("/top-products/weekly")
    @Operation(
        summary = "Get weekly top-selling products",
        description = "Retrieves the top-selling products for the current week."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved weekly top products",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 150, \"totalRevenue\": 7500.0, \"period\": \"WEEKLY\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TopProductDTO>> getWeekly(
        @Parameter(description = "Maximum number of products to return", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(TopProductsPeriod.WEEKLY, limit));
    }

    @GetMapping("/top-products/monthly")
    @Operation(
        summary = "Get monthly top-selling products",
        description = "Retrieves the top-selling products for the current month."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly top products",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 600, \"totalRevenue\": 30000.0, \"period\": \"MONTHLY\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TopProductDTO>> getMonthly(
        @Parameter(description = "Maximum number of products to return", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(TopProductsPeriod.MONTHLY, limit));
    }

    @GetMapping("/top-products/yearly")
    @Operation(
        summary = "Get yearly top-selling products",
        description = "Retrieves the top-selling products for the current year."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved yearly top products",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 7200, \"totalRevenue\": 360000.0, \"period\": \"YEARLY\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TopProductDTO>> getYearly(
        @Parameter(description = "Maximum number of products to return", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(TopProductsPeriod.YEARLY, limit));
    }

    @GetMapping("/debug/orders")
    @Operation(
        summary = "Debug endpoint to check orders and date ranges",
        description = "Debug endpoint that provides detailed information about orders and their date ranges. Useful for troubleshooting analytics data issues."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Debug information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> debugOrders() {
        return ResponseEntity.ok(analyticsService.debugOrders());
    }

    @GetMapping("/debug/database")
    @Operation(
        summary = "Debug endpoint to check database tables directly",
        description = "Debug endpoint that provides direct database table information. Useful for troubleshooting data integrity issues."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database debug information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> debugDatabase() {
        return ResponseEntity.ok(analyticsService.debugDatabase());
    }

    @GetMapping("/debug/dates")
    @Operation(
        summary = "Debug endpoint to check date calculations",
        description = "Debug endpoint that provides information about date calculations and ranges. Useful for troubleshooting date-related issues."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Date debug information retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> debugDates() {
        return ResponseEntity.ok(analyticsService.debugDateCalculation());
    }

    @GetMapping("/debug/top-products")
    @Operation(
        summary = "Debug top-products analytics for a specific period",
        description = "Returns detailed debug information about top-products analytics for the specified period to help troubleshoot issues."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Debug information retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> debugTopProducts(
        @Parameter(description = "Time period to debug", example = "DAILY", required = true)
        @RequestParam(name = "period") TopProductsPeriod period
    ) {
        try {
            Map<String, Object> debugInfo = analyticsService.debugTopProducts(period);
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get debug information");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Simple health check",
        description = "Simple health check endpoint to verify that the analytics service is running."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy",
            content = @Content(mediaType = "text/plain",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "Analytics service is running"
                )
            )
        )
    })
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics service is running");
    }

    @GetMapping("/top-products/all")
    @Operation(
        summary = "Get all top-selling products without date filter",
        description = "Retrieves all top-selling products without any date filtering. This endpoint is primarily used for debugging purposes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all top products",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TopProductDTO.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "[{\"productId\": 1, \"productName\": \"Pizza Margherita\", \"totalQuantity\": 1000, \"totalRevenue\": 50000.0, \"period\": \"ALL\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<TopProductDTO>> getAllTopProducts(
        @Parameter(description = "Maximum number of products to return", example = "10", required = false)
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(analyticsService.getAllTopProducts(limit));
    }

    // New enhanced analytics endpoints using DailySalesSummary

    @GetMapping("/revenue")
    @Operation(
        summary = "Get revenue analytics for a specific period",
        description = "Retrieves revenue analytics data for the specified period (DAILY, WEEKLY, MONTHLY, YEARLY). Uses optimized summaries for fast performance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved revenue analytics",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"totalRevenue\": 15000.0, \"orderCount\": 45, \"averageOrderValue\": 333.33, \"period\": \"DAILY\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics(
        @Parameter(description = "Time period for analytics", example = "DAILY", required = true)
        @RequestParam(name = "period") TopProductsPeriod period
    ) {
        return ResponseEntity.ok(analyticsService.getRevenueAnalytics(period));
    }

    @GetMapping("/revenue/realtime")
    @Operation(
        summary = "Get real-time revenue analytics for a specific period",
        description = "Retrieves real-time revenue analytics data for the specified period (DAILY, WEEKLY, MONTHLY, YEARLY). Calculates from actual orders, not summaries."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved real-time revenue analytics",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"totalRevenue\": 15000.0, \"totalOrders\": 45, \"averageOrderValue\": 333.33, \"period\": \"DAILY\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getRealTimeRevenueAnalytics(
        @Parameter(description = "Time period for analytics", example = "DAILY", required = true)
        @RequestParam(name = "period") TopProductsPeriod period
    ) {
        return ResponseEntity.ok(analyticsService.getRealTimeRevenueAnalytics(period));
    }

    @GetMapping("/category-sales")
    @Operation(
        summary = "Get category sales breakdown for a specific period",
        description = "Retrieves sales breakdown by product categories for the specified period. Uses optimized summaries for fast performance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved category sales breakdown",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"Pizza\": \"40%\", \"Pasta\": \"25%\", \"Salad\": \"20%\", \"Drinks\": \"15%\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> getCategorySalesBreakdown(
        @Parameter(description = "Time period for analytics", example = "DAILY", required = true)
        @RequestParam(name = "period") TopProductsPeriod period
    ) {
        return ResponseEntity.ok(analyticsService.getCategorySalesBreakdown(period));
    }

    @GetMapping("/employee-performance")
    @Operation(
        summary = "Get employee performance for a specific period",
        description = "Retrieves employee performance metrics for the specified period. Uses optimized summaries for fast performance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employee performance",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"topEmployee\": {\"employeeId\": 1, \"employeeName\": \"John Doe\", \"ordersHandled\": 25, \"totalRevenue\": 5000.0}, \"totalEmployees\": 5, \"averageOrdersPerEmployee\": 20.0}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid period parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getEmployeePerformance(
        @Parameter(description = "Time period for analytics", example = "DAILY", required = true)
        @RequestParam(name = "period") TopProductsPeriod period
    ) {
        try {
            Map<String, Object> result = analyticsService.getEmployeePerformance(period);
            System.out.println("Employee performance result: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error in getEmployeePerformance: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Manual summary generation endpoints
    @PostMapping("/generate-daily")
    @Operation(
        summary = "Manually generate daily sales summary for a specific date",
        description = "Manually generates a daily sales summary for the specified date. This endpoint is useful for creating historical data or regenerating summaries."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daily summary generated successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"message\": \"Daily summary generated successfully\", \"date\": \"2024-01-15\", \"reportType\": \"DAILY\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateDailySummary(
        @Parameter(description = "Date for which to generate summary", example = "2024-01-15", required = true)
        @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            dailySalesSummaryJobService.generateSalesSummary(date, date, "DAILY");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Daily summary generated successfully");
            response.put("date", date);
            response.put("reportType", "DAILY");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate daily summary");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("date", date);
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-weekly")
    @Operation(
        summary = "Manually generate weekly sales summary for a specific week",
        description = "Manually generates a weekly sales summary for the week ending on the specified date. The week starts on Monday and ends on Sunday."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Weekly summary generated successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"message\": \"Weekly summary generated successfully\", \"startDate\": \"2024-01-15\", \"endDate\": \"2024-01-21\", \"reportType\": \"WEEKLY\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateWeeklySummary(
        @Parameter(description = "End date of the week (Sunday)", example = "2024-01-21", required = true)
        @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            LocalDate startDate = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            dailySalesSummaryJobService.generateSalesSummary(startDate, endDate, "WEEKLY");
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Weekly summary generated successfully");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("reportType", "WEEKLY");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate weekly summary");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("endDate", endDate);
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-monthly")
    @Operation(
        summary = "Manually generate monthly sales summary for a specific month",
        description = "Manually generates a monthly sales summary for the specified year and month."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Monthly summary generated successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"message\": \"Monthly summary generated successfully\", \"startDate\": \"2024-01-01\", \"endDate\": \"2024-01-31\", \"year\": 2024, \"month\": 1, \"reportType\": \"MONTHLY\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid month (must be 1-12)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateMonthlySummary(
        @Parameter(description = "Year for the month", example = "2024", required = true)
        @RequestParam(name = "year") int year,
        @Parameter(description = "Month number (1-12)", example = "1", required = true)
        @RequestParam(name = "month") int month
    ) {
        try {
            // Validate month input
            if (month < 1 || month > 12) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid month");
                errorResponse.put("message", "Month must be between 1 and 12");
                errorResponse.put("year", year);
                errorResponse.put("month", month);
                errorResponse.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
            
            dailySalesSummaryJobService.generateSalesSummary(startDate, endDate, "MONTHLY");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Monthly summary generated successfully");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("year", year);
            response.put("month", month);
            response.put("reportType", "MONTHLY");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate monthly summary");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("year", year);
            errorResponse.put("month", month);
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-test-data")
    @Operation(
        summary = "Generate test data for today and current week/month",
        description = "Generates test sales summary data for today, current week, and current month. Useful for development and testing purposes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test data generated successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"message\": \"Test data generated successfully\", \"dailyDate\": \"2024-01-15\", \"weeklyStart\": \"2024-01-15\", \"weeklyEnd\": \"2024-01-21\", \"monthlyStart\": \"2024-01-01\", \"monthlyEnd\": \"2024-01-31\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateTestData() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
            
            // Generate daily summary for today
            dailySalesSummaryJobService.generateSalesSummary(today, today, "DAILY");
            
            // Generate weekly summary for current week
            LocalDate weekStart = weekEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            dailySalesSummaryJobService.generateSalesSummary(weekStart, weekEnd, "WEEKLY");
            
            // Generate monthly summary for current month
            LocalDate monthStart = monthEnd.with(TemporalAdjusters.firstDayOfMonth());
            dailySalesSummaryJobService.generateSalesSummary(monthStart, monthEnd, "MONTHLY");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test data generated successfully");
            response.put("dailyDate", today);
            response.put("weeklyStart", weekStart);
            response.put("weeklyEnd", weekEnd);
            response.put("monthlyStart", monthStart);
            response.put("monthlyEnd", monthEnd);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate test data");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-yearly")
    @Operation(
        summary = "Manually generate yearly sales summary for a specific year",
        description = "Manually generates a yearly sales summary for the specified year."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Yearly summary generated successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"message\": \"Yearly summary generated successfully\", \"startDate\": \"2024-01-01\", \"endDate\": \"2024-12-31\", \"year\": 2024, \"reportType\": \"YEARLY\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateYearlySummary(
        @Parameter(description = "Year for the summary", example = "2024", required = true)
        @RequestParam(name = "year") int year
    ) {
        try {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            
            dailySalesSummaryJobService.generateSalesSummary(startDate, endDate, "YEARLY");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Yearly summary generated successfully");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("year", year);
            response.put("reportType", "YEARLY");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate yearly summary");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("year", year);
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/ensure-current-summaries")
    @Operation(
        summary = "Ensure current period summaries are available",
        description = "Generates summaries for current periods (today, current week, current month) to ensure real-time analytics work properly."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current period summaries ensured successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> ensureCurrentPeriodSummaries() {
        try {
            analyticsService.ensureCurrentPeriodSummaries();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Current period summaries ensured successfully");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to ensure current period summaries");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/performance-metrics")
    @Operation(
        summary = "Get comprehensive performance metrics for all periods",
        description = "Retrieves comprehensive performance metrics including revenue, category breakdown, employee performance, and top products for all time periods (DAILY, WEEKLY, MONTHLY, YEARLY)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved performance metrics",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"daily\": {\"revenue\": {\"totalRevenue\": 15000.0}, \"categoryBreakdown\": {\"Pizza\": \"40%\"}, \"employeePerformance\": {\"topEmployee\": {\"employeeName\": \"John Doe\"}}, \"topProducts\": [{\"productName\": \"Pizza Margherita\"}]}, \"weekly\": {...}, \"monthly\": {...}, \"yearly\": {...}}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get metrics for all periods
        for (TopProductsPeriod period : TopProductsPeriod.values()) {
            Map<String, Object> periodMetrics = new HashMap<>();
            periodMetrics.put("revenue", analyticsService.getRevenueAnalytics(period));
            periodMetrics.put("categoryBreakdown", analyticsService.getCategorySalesBreakdown(period));
            periodMetrics.put("employeePerformance", analyticsService.getEmployeePerformance(period));
            periodMetrics.put("topProducts", analyticsService.getTopProducts(period, 5)); // Top 5 for overview
            
            metrics.put(period.name().toLowerCase(), periodMetrics);
        }
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/summary-status")
    @Operation(
        summary = "Check the status of sales summaries",
        description = "Returns the status of available sales summaries and their dates to help debug analytics issues."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary status retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getSummaryStatus() {
        try {
            List<DailySalesSummary> summaries = dailySalesSummaryRepository.findAllOrderByReportDateDesc();
            LocalDate today = LocalDate.now();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalSummaries", summaries.size());
            response.put("currentDate", today);
            response.put("summaries", summaries.stream()
                .map(s -> {
                    Map<String, Object> summaryInfo = new HashMap<>();
                    summaryInfo.put("id", s.getId());
                    summaryInfo.put("reportDate", s.getReportDate());
                    summaryInfo.put("reportType", s.getReportType());
                    summaryInfo.put("periodStartDate", s.getPeriodStartDate());
                    summaryInfo.put("periodEndDate", s.getPeriodEndDate());
                    summaryInfo.put("totalOrders", s.getTotalOrders());
                    summaryInfo.put("totalRevenue", s.getTotalRevenue());
                    summaryInfo.put("topProductsCount", s.getTopProductsCount());
                    return summaryInfo;
                })
                .collect(Collectors.toList()));
            
            // Check current period summaries
            Map<String, Boolean> currentPeriods = new HashMap<>();
            currentPeriods.put("daily", summaries.stream()
                .anyMatch(s -> s.getReportType().equals("DAILY") && s.getReportDate().equals(today)));
            currentPeriods.put("weekly", summaries.stream()
                .anyMatch(s -> s.getReportType().equals("WEEKLY") && 
                    s.getPeriodStartDate().equals(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))));
            currentPeriods.put("monthly", summaries.stream()
                .anyMatch(s -> s.getReportType().equals("MONTHLY") && 
                    s.getPeriodStartDate().equals(today.withDayOfMonth(1))));
            currentPeriods.put("yearly", summaries.stream()
                .anyMatch(s -> s.getReportType().equals("YEARLY") && 
                    s.getPeriodStartDate().equals(today.withDayOfYear(1))));
            
            response.put("currentPeriodSummaries", currentPeriods);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get summary status");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/realtime-stats")
    @Operation(
        summary = "Get real-time statistics for dashboard",
        description = "Retrieves real-time statistics including today's orders, revenue, and active reservations."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved real-time statistics",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success Response",
                    value = "{\"todayOrders\": 15, \"todayRevenue\": 2500.50, \"activeReservations\": 3, \"weeklyOrders\": 85, \"weeklyRevenue\": 12500.75, \"monthlyOrders\": 320, \"monthlyRevenue\": 45000.25}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getRealtimeStats() {
        try {
            Map<String, Object> stats = analyticsService.getRealtimeStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Real-time statistics could not be retrieved");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}


