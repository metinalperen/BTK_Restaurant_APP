package com.example.demo.controller;

import com.example.demo.dto.response.ActivityLogResponseDTO;
import com.example.demo.model.ActivityLog;
import com.example.demo.service.ActivityLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Tag(
        name = "Activity Log",
        description = "API for managing activity logs including retrieval by various criteria such as user, entity, date range, and action type."
)
@RestController
@RequestMapping("/api/activity-logs")
@CrossOrigin(origins = "*")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @Operation(
            summary = "Get all activity logs",
            description = "Retrieves a list of all activity logs."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getAllActivityLogs() {
        try {
            List<ActivityLog> logs = activityLogService.getAllActivityLogs();
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get activity logs by user",
            description = "Retrieves activity logs for a specific user."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getActivityLogsByUser(
            @Parameter(description = "ID of the user to retrieve logs for", required = true)
            @PathVariable Long userId) {
        try {
            List<ActivityLog> logs = activityLogService.getActivityLogsByUser(userId);
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(
            summary = "Get activity logs by entity",
            description = "Retrieves activity logs for a specific entity (e.g., Order, Product)."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getActivityLogsByEntity(
            @Parameter(description = "Type of the entity (e.g., 'Order', 'Product')", required = true)
            @PathVariable String entityType,
            @Parameter(description = "ID of the entity", required = true)
            @PathVariable Long entityId) {
        try {
            List<ActivityLog> logs = activityLogService.getActivityLogsByEntity(entityType, entityId.intValue());
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/date-range")
    @Operation(
            summary = "Get activity logs by date range",
            description = "Retrieves activity logs within a specified date range."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getActivityLogsByDateRange(
            @Parameter(description = "Start date for the range (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam String startDate,
            @Parameter(description = "End date for the range (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate, DATE_TIME_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate, DATE_TIME_FORMATTER);
            List<ActivityLog> logs = activityLogService.getActivityLogsByDateRange(start, end);
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/action/{actionType}")
    @Operation(
            summary = "Get activity logs by action type",
            description = "Retrieves activity logs for a specific action type (e.g., CREATE, UPDATE, DELETE)."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getActivityLogsByActionType(
            @Parameter(description = "Action type to filter by (e.g., 'CREATE', 'UPDATE')", required = true)
            @PathVariable String actionType) {
        try {
            List<ActivityLog> logs = activityLogService.getActivityLogsByActionType(actionType);
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bootstrap-admin")
    @Operation(
            summary = "Get bootstrap admin activity logs",
            description = "Retrieves all activity logs related to bootstrap admin operations."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getBootstrapAdminLogs() {
        try {
            List<ActivityLog> logs = activityLogService.getBootstrapAdminLogs();
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent")
    @Operation(
            summary = "Get recent activities",
            description = "Retrieves a list of recent activity logs."
    )
    public ResponseEntity<List<ActivityLogResponseDTO>> getRecentActivities() {
        try {
            List<ActivityLog> logs = activityLogService.getAllActivityLogs(); // You can add a limit here
            List<ActivityLogResponseDTO> responseDTOs = logs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ActivityLogResponseDTO mapToResponseDTO(ActivityLog activityLog) {
        ActivityLogResponseDTO dto = new ActivityLogResponseDTO();
        dto.setId(activityLog.getId());
        dto.setActionType(activityLog.getActionType());
        dto.setEntityType(activityLog.getEntityType());
        dto.setEntityId(activityLog.getEntityId());
        dto.setCreatedAt(activityLog.getCreatedAt());
        
        if (activityLog.getDetails() != null) {
            dto.setDetails(activityLog.getDetails().toString());
        }
        
        if (activityLog.getUser() != null) {
            dto.setUserName(activityLog.getUser().getName());
            dto.setUserId(activityLog.getUser().getId());
        }
        
        return dto;
    }
}