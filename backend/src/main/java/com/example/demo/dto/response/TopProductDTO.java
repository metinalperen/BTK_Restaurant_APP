package com.example.demo.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Frontend grafiği için bir satır: ürün ve metrikleri.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Top selling product data transfer object")
public class TopProductDTO {
    @Schema(description = "Unique identifier of the product", example = "1")
    private Long productId;
    
    @Schema(description = "Name of the product", example = "Pizza Margherita")
    private String productName;
    
    @Schema(description = "Total quantity sold", example = "25")
    private long totalQuantity;
    
    @Schema(description = "Number of different orders containing this product", example = "15")
    private long orderCount;
    
    @Schema(description = "Total revenue generated from this product", example = "1250.00")
    private BigDecimal totalRevenue;
}


