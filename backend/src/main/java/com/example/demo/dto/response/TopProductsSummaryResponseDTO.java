package com.example.demo.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tek bir çağrıda günlük/haftalık/aylık/yıllık listeleri birlikte döndürmek için.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary response containing top products for all time periods")
public class TopProductsSummaryResponseDTO {
    @Schema(description = "Top products for the current day")
    private List<TopProductDTO> daily;
    
    @Schema(description = "Top products for the current week")
    private List<TopProductDTO> weekly;
    
    @Schema(description = "Top products for the current month")
    private List<TopProductDTO> monthly;
    
    @Schema(description = "Top products for the current year")
    private List<TopProductDTO> yearly;
}


