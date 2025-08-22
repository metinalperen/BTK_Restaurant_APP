package com.example.demo.exception.analytics;

import com.example.demo.enums.TopProductsPeriod;
import java.time.LocalDate;

/**
 * Exception thrown when a required analytics summary is not found
 */
public class SummaryNotFoundException extends RuntimeException {
    
    private final LocalDate reportDate;
    private final String reportType;
    private final TopProductsPeriod period;
    
    public SummaryNotFoundException(LocalDate reportDate, String reportType) {
        super(String.format("Summary not found for date: %s and type: %s", reportDate, reportType));
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.period = null;
    }
    
    public SummaryNotFoundException(TopProductsPeriod period, LocalDate reportDate) {
        super(String.format("Summary not found for period: %s and date: %s", period, reportDate));
        this.reportDate = reportDate;
        this.reportType = null;
        this.period = period;
    }
    
    // Getters
    public LocalDate getReportDate() { return reportDate; }
    public String getReportType() { return reportType; }
    public TopProductsPeriod getPeriod() { return period; }
}
