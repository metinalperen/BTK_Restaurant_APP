package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Dining table response data transfer object")
public class DiningTableResponseDto {
    @Schema(description = "Unique identifier of the dining table", example = "1")
    private Long id;
    
    @Schema(description = "Table number", example = "5")
    private Integer tableNumber;
    
    @Schema(description = "Table capacity (number of seats)", example = "4")
    private Integer capacity;
    
    @Schema(description = "Status ID of the table", example = "1")
    private Long statusId;
    
    @Schema(description = "Status name of the table", example = "AVAILABLE")
    private String statusName;
    
    @Schema(description = "Salon ID where the table is located", example = "1")
    private Long salonId;
    
    @Schema(description = "Salon name where the table is located", example = "Ana Salon")
    private String salonName;
    
    @Schema(description = "Whether the table has an active order", example = "false")
    private boolean hasActiveOrder;
    
    @Schema(description = "Whether the table has a completed order", example = "true")
    private boolean hasCompletedOrder;
    
    @Schema(description = "Real status calculated based on order status", example = "OCCUPIED")
    private String realStatus;
    
    @Schema(description = "Number of items in the active order", example = "3")
    private int activeOrderItemsCount;
}
