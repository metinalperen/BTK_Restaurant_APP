package com.example.demo.exception.analytics;

import com.example.demo.enums.TopProductsPeriod;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Exception thrown when summary generation takes too long
 */
public class SummaryGenerationTimeoutException extends RuntimeException {
    
    private final Duration timeout;
    private final TopProductsPeriod period;
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    public SummaryGenerationTimeoutException(Duration timeout, TopProductsPeriod period, 
                                          LocalDate startDate, LocalDate endDate) {
        super(String.format("Summary generation timed out after %s for period: %s (%s to %s)", 
                           timeout, period, startDate, endDate));
        this.timeout = timeout;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters
    public Duration getTimeout() { return timeout; }
    public TopProductsPeriod getPeriod() { return period; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
