package com.example.demo.service;


import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;
import com.example.demo.model.ActivityLog;
import com.example.demo.repository.ActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityLogService {

    // Constants for bootstrap admin action types
    public static final String BOOTSTRAP_ADMIN_ATTEMPT = "BOOTSTRAP_ADMIN_ATTEMPT";
    public static final String BOOTSTRAP_ADMIN_CREATED = "BOOTSTRAP_ADMIN_CREATED";
    public static final String BOOTSTRAP_ADMIN_EMAIL_SENT = "BOOTSTRAP_ADMIN_EMAIL_SENT";
    public static final String BOOTSTRAP_ADMIN_EMAIL_FAILED = "BOOTSTRAP_ADMIN_EMAIL_FAILED";

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }
    // CREATE: Log activity with ActivityLog object
    public ActivityLog logActivity(ActivityLog activityLog) {
        if (activityLog.getUser() == null) {
            getCurrentAuthenticatedUser().ifPresent(activityLog::setUser);
        }
        return activityLogRepository.save(activityLog);
    }


    // CREATE: Log a new activity (string details)
    public ActivityLog logActivity(String actionType, String entityType, Long entityId, String details) {
        ActivityLog activityLog = new ActivityLog(actionType, entityType, entityId, details);
        // Attach user from SecurityContext if present
        getCurrentAuthenticatedUser().ifPresent(activityLog::setUser);
        return activityLogRepository.save(activityLog);
    }

    // CREATE: Log activity with structured details
    public ActivityLog logActivity(String actionType, String entityType, Long entityId, ObjectNode details) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setActionType(actionType);
        activityLog.setEntityType(entityType);
        activityLog.setEntityId(entityId);
        activityLog.setDetails(details);
        activityLog.setCreatedAt(LocalDateTime.now());
        // Attach user from SecurityContext if present
        getCurrentAuthenticatedUser().ifPresent(activityLog::setUser);
        return activityLogRepository.save(activityLog);
    }


    /**
     * Log activity with explicit user id and structured details (preferred).
     * Ensures the actor user is written to activity_logs.user_id.
     */
    public ActivityLog logActivity(Long userId, String actionType, String entityType, Long entityId, ObjectNode details) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setActionType(actionType);
        activityLog.setEntityType(entityType);
        activityLog.setEntityId(entityId);
        activityLog.setDetails(details);
        activityLog.setCreatedAt(LocalDateTime.now());

        // Attach explicit user if provided; otherwise gracefully fall back to SecurityContext (if any)
        if (userId != null) {
            loadUserById(userId).ifPresent(activityLog::setUser);
        } else {
            getCurrentAuthenticatedUser().ifPresent(activityLog::setUser);
        }

        return activityLogRepository.save(activityLog);
    }

    // CREATE: Log activity with an explicit user id and string details
    public ActivityLog logUserActivity(Long userId, String actionType, String entityType, Long entityId, String details) {
        ActivityLog activityLog = new ActivityLog(actionType, entityType, entityId, details);
        // Prefer explicit user id provided
        loadUserById(userId).ifPresent(activityLog::setUser);
        return activityLogRepository.save(activityLog);
    }


    // READ: Get all logs
    @Transactional(readOnly = true)
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogRepository.findAll();
    }

    // READ: Get logs by user
    @Transactional(readOnly = true)
    public List<ActivityLog> getActivityLogsByUser(Long userId) {
        return activityLogRepository.findByUserId(userId);
    }

    // READ: Get logs by entity
    @Transactional(readOnly = true)
    public List<ActivityLog> getActivityLogsByEntity(String entityType, Integer entityId) {
        return activityLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    // READ: Get logs by date range
    @Transactional(readOnly = true)
    public List<ActivityLog> getActivityLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return activityLogRepository.findByCreatedAtBetween(startDate, endDate);
    }

    // READ: Get logs by action type
    @Transactional(readOnly = true)
    public List<ActivityLog> getActivityLogsByActionType(String actionType) {
        return activityLogRepository.findByActionType(actionType);
    }

    // READ: Get bootstrap admin related logs
    @Transactional(readOnly = true)
    public List<ActivityLog> getBootstrapAdminLogs() {
        List<ActivityLog> allLogs = activityLogRepository.findAll();
        return allLogs.stream()
                .filter(log -> log.getActionType() != null && 
                        (log.getActionType().startsWith("BOOTSTRAP_ADMIN") || 
                         log.getActionType().equals("BOOTSTRAP_ADMIN_ATTEMPT")))
                .collect(Collectors.toList());
    }

    // Helper method to create structured details
    public ObjectNode createDetailsNode(String message) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", message);
        details.put("timestamp", LocalDateTime.now().toString());
        return details;
    }

    // Helper method to create structured details with additional fields
    public ObjectNode createDetailsNode(String message, String... additionalFields) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", message);
        details.put("timestamp", LocalDateTime.now().toString());
        
        for (int i = 0; i < additionalFields.length; i += 2) {
            if (i + 1 < additionalFields.length) {
                details.put(additionalFields[i], additionalFields[i + 1]);
            }
        }
        
        return details;
    }

    /**
     * Create an empty ObjectNode for custom details
     */
    public ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    // NEW METHOD: Log phone number changes with structured details
    public ActivityLog logPhoneNumberChange(Long userId, String oldPhoneNumber, String newPhoneNumber) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", "Phone number updated");
        details.put("oldPhoneNumber", oldPhoneNumber != null ? oldPhoneNumber : "");
        details.put("newPhoneNumber", newPhoneNumber != null ? newPhoneNumber : "");
        details.put("timestamp", LocalDateTime.now().toString());

        return logActivity("PHONE_UPDATE", "USER", userId, details);
    }

    // NEW METHOD: Log password resets with structured details
    public ActivityLog logPasswordReset(Long userId, String userEmail) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", "Password reset completed");
        details.put("userEmail", userEmail);
        details.put("timestamp", LocalDateTime.now().toString());

        return logActivity("PASSWORD_RESET", "USER", userId, details);
    }

    // NEW METHOD: Log bootstrap admin activities with structured details
    public ActivityLog logBootstrapAdminActivity(String actionType, Long userId, String adminName, String adminEmail, String additionalMessage, String... additionalFields) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", additionalMessage);
        details.put("adminName", adminName != null ? adminName : "");
        details.put("adminEmail", adminEmail != null ? adminEmail : "");
        details.put("timestamp", LocalDateTime.now().toString());
        
        for (int i = 0; i < additionalFields.length; i += 2) {
            if (i + 1 < additionalFields.length) {
                details.put(additionalFields[i], additionalFields[i + 1]);
            }
        }
        
        return logActivity(userId, actionType, "USER", userId, details);
    }

    // NEW METHOD: Log system-level bootstrap admin attempts (when no user context)
    public ActivityLog logBootstrapAdminSystemActivity(String actionType, String message, String... additionalFields) {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("message", message);
        details.put("timestamp", LocalDateTime.now().toString());
        
        for (int i = 0; i < additionalFields.length; i += 2) {
            if (i + 1 < additionalFields.length) {
                details.put(additionalFields[i], additionalFields[i + 1]);
            }
        }
        
        return logActivity(actionType, "SYSTEM", null, details);
    }

    /**
     * Log activity with explicit user id and plain string details.
     * Converts the string to JSON, then persists.
     */
    public ActivityLog logActivity(Long userId, String actionType, String entityType, Long entityId, String details) {
        ActivityLog activityLog = new ActivityLog(actionType, entityType, entityId, details);

        if (userId != null) {
            loadUserById(userId).ifPresent(activityLog::setUser);
        } else {
            getCurrentAuthenticatedUser().ifPresent(activityLog::setUser);
        }

        return activityLogRepository.save(activityLog);
    }
    private Optional<User> getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();

        Object principal = authentication.getPrincipal();
        String usernameOrEmail;
        if (principal instanceof UserDetails) {
            usernameOrEmail = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            usernameOrEmail = (String) principal;
        } else {
            return Optional.empty();
        }
        return userRepository.findByEmail(usernameOrEmail);
    }

    private Optional<User> loadUserById(Long userId) {
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }
}