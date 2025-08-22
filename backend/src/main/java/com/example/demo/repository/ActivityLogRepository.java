package com.example.demo.repository;

import com.example.demo.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Find by user ID
    List<ActivityLog> findByUserId(Long userId);

    // Find by entity type and ID
    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, Integer entityId);

    // Find by date range
    List<ActivityLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by action type
    List<ActivityLog> findByActionType(String actionType);

    // Find by entity type
    List<ActivityLog> findByEntityType(String entityType);

    // Custom query for recent activities
    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivities();

    // Custom query for user activities with limit
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentUserActivities(@Param("userId") Long userId);
}