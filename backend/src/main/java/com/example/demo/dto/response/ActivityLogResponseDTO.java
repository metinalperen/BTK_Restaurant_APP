package com.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ActivityLogResponseDTO {
    @Schema(description = "Unique identifier for the activity log entry", example = "1")
    private Long id;

    @Schema(description = "Type of action performed", example = "CREATE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String actionType;

    @Schema(description = "Type of entity affected by the action", example = "ORDER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entityType;

    @Schema(description = "Unique identifier of the entity affected by the action", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long entityId;

    @Schema(description = "Details of the action performed", example = "Order created successfully", requiredMode = Schema.RequiredMode.REQUIRED)
    private String details;

    @Schema(description = "Timestamp when the activity log entry was created", example = "2023-10-01T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "Username of the user who performed the action", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
    
    @Schema(description = "Unique identifier of the user who performed the action", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
