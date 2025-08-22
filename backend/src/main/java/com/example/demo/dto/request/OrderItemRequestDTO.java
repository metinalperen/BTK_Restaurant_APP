package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @Schema(description = "Ürün ID'si", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "Sipariş miktarı", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;

    @Schema(description = "Notlar", example = "Az pişmiş, bol soslu", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String note;
}
