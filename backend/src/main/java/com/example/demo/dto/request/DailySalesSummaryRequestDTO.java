package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class DailySalesSummaryRequestDTO {

    @Schema(description = "Günlük satış raporu tarihi", type = "string", format = "date", example = "2023-10-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "Toplam satış miktarı", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    public LocalDate getReportDate() {
        return reportDate;
    }

    @Schema(description = "Toplam satış miktarı", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }
}
