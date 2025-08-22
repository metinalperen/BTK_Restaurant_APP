package com.example.demo.exception.analytics;

import com.example.demo.enums.TopProductsPeriod;
import java.time.LocalDate;

/**
 * Exception thrown when analytics summary generation fails
 */
public class AnalyticsGenerationException extends RuntimeException {
    
    private final TopProductsPeriod period;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String reportType;
    
    public AnalyticsGenerationException(String message, TopProductsPeriod period, 
                                     LocalDate startDate, LocalDate endDate, String reportType) {
        super(message);
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportType = reportType;
    }
    
    public AnalyticsGenerationException(String message, TopProductsPeriod period, 
                                     LocalDate startDate, LocalDate endDate, String reportType, 
                                     Throwable cause) {
        super(message, cause);
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reportType = reportType;
    }
    
    // Getters
    public TopProductsPeriod getPeriod() { return period; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReportType() { return reportType; }
}
