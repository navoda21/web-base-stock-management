package com.stockmanagement.service;

import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.AuditLog;
import com.stockmanagement.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportsService {
    // Basic statistics
    Map<String, Object> getSummaryStatistics();

    Map<String, Object> getActivityChartData(int days);

    Map<ActionType, Long> getActionTypeCounts();

    // Enhanced audit log queries with proper filtering
    Page<AuditLog> getAuditLogs(ActionType actionType, String tableName,
                                LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<AuditLog> getUserActivity(Long userId, Pageable pageable);

    List<AuditLog> getRecentActivity(int limit);

    // New analytics functions
    Map<String, Object> getUserStatistics();

    Map<String, Object> getSecurityAnalytics();

    Map<String, Object> getPerformanceMetrics();

    List<Map<String, Object>> getTopActiveUsers(int limit);

    Map<String, Long> getActivityByHour();

    Map<String, Long> getActivityByDay();

    List<Map<String, Object>> getFailedLoginAttempts(int days);

    Map<UserRole, Long> getUserDistributionByRole();

    // Advanced filtering and search
    Page<AuditLog> getFilteredAuditLogs(Map<String, Object> filters, Pageable pageable);

    List<Map<String, Object>> getSystemHealthMetrics();

    // Enhanced PDF generation
    byte[] generateSummaryPdf();

    byte[] generateDetailedAuditReport(LocalDateTime startDate, LocalDateTime endDate);

    byte[] generateUserActivityReport(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    byte[] generateSecurityReport(int days);
}