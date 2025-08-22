package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RoleRequestDTO {
    @Schema(description = "Rol ID'si", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
