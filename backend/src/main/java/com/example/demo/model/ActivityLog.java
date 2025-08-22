package com.example.demo.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructor without id for creating new logs
    public ActivityLog(String actionType, String entityType, Long entityId, String details) {
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = parseDetails(details);
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLog(User user, String actionType, String entityType, Long entityId, String details) {
        this.user = user;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = parseDetails(details);
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    private JsonNode parseDetails(String details) {
        if (details == null || details.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(details);
        } catch (Exception e) {
            // If parsing fails, create a simple JSON with the string as value
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.createObjectNode().put("message", details);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public String getDetailsAsString() {
        if (details == null) {
            return null;
        }
        return details.toString();
    }
}