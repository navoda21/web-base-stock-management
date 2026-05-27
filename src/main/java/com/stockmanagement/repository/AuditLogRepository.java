package com.stockmanagement.repository;

import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.AuditLog;
import com.stockmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Basic queries
    Page<AuditLog> findByUser(User user, Pageable pageable);

    Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable);

    Page<AuditLog> findByTableName(String tableName, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.actionType = :actionType ORDER BY a.createdAt DESC")
    List<AuditLog> findByUserAndActionType(@Param("user") User user, @Param("actionType") ActionType actionType);

    // Enhanced queries for analytics
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:tableName IS NULL OR a.tableName = :tableName) AND " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findWithFilters(@Param("actionType") ActionType actionType,
                                   @Param("tableName") String tableName,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    // User activity analytics
    @Query("SELECT u.username, COUNT(a) as activityCount FROM AuditLog a " +
            "JOIN a.user u WHERE a.createdAt >= :since " +
            "GROUP BY u.userId, u.username ORDER BY activityCount DESC")
    List<Object[]> findTopActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT a.user, COUNT(a) as count FROM AuditLog a " +
            "WHERE a.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY a.user ORDER BY count DESC")
    List<Object[]> findUserActivityCounts(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);

    // Time-based analytics
    @Query("SELECT HOUR(a.createdAt) as hour, COUNT(a) as count FROM AuditLog a " +
            "WHERE a.createdAt >= :since GROUP BY HOUR(a.createdAt) ORDER BY hour")
    List<Object[]> findActivityByHour(@Param("since") LocalDateTime since);

    @Query("SELECT DAYNAME(a.createdAt) as dayName, COUNT(a) as count FROM AuditLog a " +
            "WHERE a.createdAt >= :since GROUP BY DAYNAME(a.createdAt), DAYOFWEEK(a.createdAt) " +
            "ORDER BY DAYOFWEEK(a.createdAt)")
    List<Object[]> findActivityByDay(@Param("since") LocalDateTime since);

    // Security analytics
    @Query("SELECT a FROM AuditLog a WHERE a.actionType = 'LOGIN' AND " +
            "a.createdAt >= :since AND a.newValues LIKE '%failed%' " +
            "ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedLoginAttempts(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT a.ipAddress) FROM AuditLog a WHERE a.createdAt >= :since")
    Long countUniqueIpAddresses(@Param("since") LocalDateTime since);

    @Query("SELECT a.ipAddress, COUNT(a) as count FROM AuditLog a " +
            "WHERE a.createdAt >= :since GROUP BY a.ipAddress ORDER BY count DESC")
    List<Object[]> findActivityByIpAddress(@Param("since") LocalDateTime since, Pageable pageable);

    // System metrics
    @Query("SELECT a.tableName, COUNT(a) as count FROM AuditLog a " +
            "WHERE a.createdAt >= :since AND a.tableName IS NOT NULL " +
            "GROUP BY a.tableName ORDER BY count DESC")
    List<Object[]> findMostModifiedTables(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since")
    Long countRecentActivity(@Param("since") LocalDateTime since);
}
