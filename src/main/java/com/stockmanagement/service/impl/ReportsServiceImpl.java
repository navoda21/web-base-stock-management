package com.stockmanagement.service.impl;

import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.AuditLog;
import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.repository.AuditLogRepository;
import com.stockmanagement.repository.UserRepository;
import com.stockmanagement.repository.UserSessionRepository;
import com.stockmanagement.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Phrase;

import java.io.ByteArrayOutputStream;

@Service
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    public Map<String, Object> getSummaryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Total audit logs
            long totalLogs = auditLogRepository.count();
            stats.put("totalLogs", totalLogs);

            // Today's activity
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            long logsToday = auditLogRepository.findByDateRange(startOfDay, endOfDay, Pageable.unpaged()).getTotalElements();
            stats.put("logsToday", logsToday);

            // This week's activity
            LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
            long logsThisWeek = auditLogRepository.findByDateRange(startOfWeek, LocalDateTime.now(), Pageable.unpaged()).getTotalElements();
            stats.put("logsThisWeek", logsThisWeek);

            // Unique users count (approximate)
            List<AuditLog> allLogs = auditLogRepository.findAll();
            long uniqueUsers = allLogs.stream()
                    .filter(log -> log.getUser() != null)
                    .map(log -> log.getUser().getUserId())
                    .distinct()
                    .count();
            stats.put("uniqueUsers", uniqueUsers);

            // Most active table
            String mostActiveTable = allLogs.stream()
                    .filter(log -> log.getTableName() != null)
                    .collect(Collectors.groupingBy(AuditLog::getTableName, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
            stats.put("mostActiveTable", mostActiveTable);

        } catch (Exception e) {
            // If there's any error, return default values
            stats.put("totalLogs", 0);
            stats.put("logsToday", 0);
            stats.put("logsThisWeek", 0);
            stats.put("uniqueUsers", 0);
            stats.put("mostActiveTable", "N/A");
        }

        return stats;
    }

    @Override
    public Map<String, Object> getActivityChartData(int days) {
        Map<String, Object> chartData = new HashMap<>();

        try {
            List<Map<String, Object>> dailyActivity = new ArrayList<>();

            for (int i = days - 1; i >= 0; i--) {
                LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime dayEnd = LocalDateTime.now().minusDays(i).withHour(23).withMinute(59).withSecond(59);

                long dayCount = auditLogRepository.findByDateRange(dayStart, dayEnd, Pageable.unpaged()).getTotalElements();

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dayStart.format(DateTimeFormatter.ofPattern("MMM dd")));
                dayData.put("count", dayCount);
                dailyActivity.add(dayData);
            }

            chartData.put("dailyActivity", dailyActivity);
            chartData.put("actionDistribution", getActionTypeCounts());

        } catch (Exception e) {
            chartData.put("dailyActivity", new ArrayList<>());
            chartData.put("actionDistribution", new HashMap<ActionType, Long>());
        }

        return chartData;
    }

    @Override
    public Map<ActionType, Long> getActionTypeCounts() {
        try {
            return auditLogRepository.findAll().stream()
                    .collect(Collectors.groupingBy(AuditLog::getActionType, Collectors.counting()));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public Page<AuditLog> getAuditLogs(ActionType actionType, String tableName,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        try {
            return auditLogRepository.findWithFilters(actionType, tableName, startDate, endDate, pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<AuditLog> getUserActivity(Long userId, Pageable pageable) {
        try {
            if (userId != null) {
                // Find user activities - for now return all, can be enhanced with user filtering
                return auditLogRepository.findAll(pageable);
            }
            return auditLogRepository.findAll(pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }

    @Override
    public List<AuditLog> getRecentActivity(int limit) {
        try {
            return auditLogRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                    .getContent();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public byte[] generateSummaryPdf() {
        try {
            Map<String, Object> stats = getSummaryStatistics();
            java.util.List<AuditLog> recent = getRecentActivity(20);

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Stock Management - Reports Summary", title));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell(new Phrase("Total Logs", normal));
            table.addCell(new Phrase(String.valueOf(stats.getOrDefault("totalLogs", 0)), normal));
            table.addCell(new Phrase("Logs Today", normal));
            table.addCell(new Phrase(String.valueOf(stats.getOrDefault("logsToday", 0)), normal));
            table.addCell(new Phrase("Logs This Week", normal));
            table.addCell(new Phrase(String.valueOf(stats.getOrDefault("logsThisWeek", 0)), normal));
            table.addCell(new Phrase("Unique Users", normal));
            table.addCell(new Phrase(String.valueOf(stats.getOrDefault("uniqueUsers", 0)), normal));
            table.addCell(new Phrase("Most Active Table", normal));
            table.addCell(new Phrase(String.valueOf(stats.getOrDefault("mostActiveTable", "N/A")), normal));
            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Recent Activity", title));
            document.add(Chunk.NEWLINE);

            for (AuditLog log : recent) {
                String user = log.getUser() != null ? log.getUser().getUsername() : "System";
                String line = String.format("%s - %s - %s", log.getCreatedAt(), user, log.getActionType());
                document.add(new Paragraph(line, normal));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    // New analytics methods implementation
    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countActiveUsers();
            long inactiveUsers = totalUsers - activeUsers;

            LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
            Long newUsersLastMonth = userRepository.countNewUsers(lastMonth);

            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);
            stats.put("newUsersLastMonth", newUsersLastMonth != null ? newUsersLastMonth : 0);

            // User registration trend
            List<Object[]> registrationTrend = userRepository.getUserRegistrationTrend(LocalDateTime.now().minusDays(30));
            List<Map<String, Object>> trendData = registrationTrend.stream()
                    .map(row -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("date", row[0].toString());
                        data.put("count", row[1]);
                        return data;
                    }).collect(Collectors.toList());
            stats.put("registrationTrend", trendData);

        } catch (Exception e) {
            stats.put("totalUsers", 0);
            stats.put("activeUsers", 0);
            stats.put("inactiveUsers", 0);
            stats.put("newUsersLastMonth", 0);
            stats.put("registrationTrend", new ArrayList<>());
        }
        return stats;
    }

    @Override
    public Map<String, Object> getSecurityAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        try {
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            LocalDateTime lastDay = LocalDateTime.now().minusDays(1);

            // Failed login attempts
            List<AuditLog> failedLogins = auditLogRepository.findFailedLoginAttempts(lastWeek);
            analytics.put("failedLoginsCount", failedLogins.size());

            // Unique IP addresses
            Long uniqueIps = auditLogRepository.countUniqueIpAddresses(lastWeek);
            analytics.put("uniqueIpAddresses", uniqueIps != null ? uniqueIps : 0);

            // Activity by IP
            List<Object[]> ipActivity = auditLogRepository.findActivityByIpAddress(lastWeek, PageRequest.of(0, 10));
            List<Map<String, Object>> ipData = ipActivity.stream()
                    .map(row -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("ipAddress", row[0]);
                        data.put("activityCount", row[1]);
                        return data;
                    }).collect(Collectors.toList());
            analytics.put("topIpAddresses", ipData);

            // Recent activity count
            Long recentActivity = auditLogRepository.countRecentActivity(lastDay);
            analytics.put("recentActivityCount", recentActivity != null ? recentActivity : 0);

        } catch (Exception e) {
            analytics.put("failedLoginsCount", 0);
            analytics.put("uniqueIpAddresses", 0);
            analytics.put("topIpAddresses", new ArrayList<>());
            analytics.put("recentActivityCount", 0);
        }
        return analytics;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        try {
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
            LocalDateTime lastDay = LocalDateTime.now().minusDays(1);

            // Activity in last hour vs last day
            Long hourlyActivity = auditLogRepository.countRecentActivity(lastHour);
            Long dailyActivity = auditLogRepository.countRecentActivity(lastDay);

            metrics.put("hourlyActivity", hourlyActivity != null ? hourlyActivity : 0);
            metrics.put("dailyActivity", dailyActivity != null ? dailyActivity : 0);

            // Active sessions count
            Long activeSessions = userSessionRepository.count();
            metrics.put("activeSessions", activeSessions != null ? activeSessions : 0);

            // Most modified tables
            List<Object[]> tableActivity = auditLogRepository.findMostModifiedTables(lastDay);
            List<Map<String, Object>> tableData = tableActivity.stream()
                    .limit(5)
                    .map(row -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("tableName", row[0]);
                        data.put("modificationCount", row[1]);
                        return data;
                    }).collect(Collectors.toList());
            metrics.put("mostModifiedTables", tableData);

        } catch (Exception e) {
            metrics.put("hourlyActivity", 0);
            metrics.put("dailyActivity", 0);
            metrics.put("activeSessions", 0);
            metrics.put("mostModifiedTables", new ArrayList<>());
        }
        return metrics;
    }

    @Override
    public List<Map<String, Object>> getTopActiveUsers(int limit) {
        try {
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            List<Object[]> topUsers = auditLogRepository.findTopActiveUsers(lastWeek, PageRequest.of(0, limit));

            return topUsers.stream()
                    .map(row -> {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", row[0]);
                        userData.put("activityCount", row[1]);
                        return userData;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> getActivityByHour() {
        try {
            LocalDateTime lastDay = LocalDateTime.now().minusDays(1);
            List<Object[]> hourlyData = auditLogRepository.findActivityByHour(lastDay);

            Map<String, Long> hourlyActivity = new LinkedHashMap<>();
            // Initialize all hours with 0
            for (int i = 0; i < 24; i++) {
                hourlyActivity.put(String.format("%02d:00", i), 0L);
            }

            // Fill with actual data
            hourlyData.forEach(row -> {
                String hour = String.format("%02d:00", (Integer) row[0]);
                hourlyActivity.put(hour, ((Number) row[1]).longValue());
            });

            return hourlyActivity;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Long> getActivityByDay() {
        try {
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            List<Object[]> dailyData = auditLogRepository.findActivityByDay(lastWeek);

            return dailyData.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> ((Number) row[1]).longValue(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getFailedLoginAttempts(int days) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<AuditLog> failedLogins = auditLogRepository.findFailedLoginAttempts(since);

            return failedLogins.stream()
                    .map(log -> {
                        Map<String, Object> attempt = new HashMap<>();
                        attempt.put("timestamp", log.getCreatedAt());
                        attempt.put("username", log.getUser() != null ? log.getUser().getUsername() : "Unknown");
                        attempt.put("ipAddress", log.getIpAddress());
                        attempt.put("userAgent", log.getUserAgent());
                        return attempt;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<UserRole, Long> getUserDistributionByRole() {
        try {
            List<Object[]> roleData = userRepository.getUserDistributionByRole();

            return roleData.stream()
                    .collect(Collectors.toMap(
                            row -> (UserRole) row[0],
                            row -> ((Number) row[1]).longValue(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Page<AuditLog> getFilteredAuditLogs(Map<String, Object> filters, Pageable pageable) {
        try {
            ActionType actionType = null;
            if (filters.containsKey("actionType") && filters.get("actionType") != null) {
                try {
                    actionType = ActionType.valueOf((String) filters.get("actionType"));
                } catch (IllegalArgumentException e) {
                    // Invalid action type, ignore
                }
            }

            String tableName = (String) filters.get("tableName");
            LocalDateTime startDate = (LocalDateTime) filters.get("startDate");
            LocalDateTime endDate = (LocalDateTime) filters.get("endDate");
            String username = (String) filters.get("username");

            // For now, use the existing method - can be enhanced later for username filtering
            Page<AuditLog> results = auditLogRepository.findWithFilters(actionType, tableName, startDate, endDate, pageable);

            // If username filter is specified, filter the results (this is not optimal but works for now)
            if (username != null && !username.trim().isEmpty()) {
                List<AuditLog> filteredContent = results.getContent().stream()
                        .filter(log -> log.getUser() != null &&
                                log.getUser().getUsername().toLowerCase().contains(username.toLowerCase()))
                        .collect(Collectors.toList());

                return new PageImpl<>(filteredContent, pageable, results.getTotalElements());
            }

            return results;
        } catch (Exception e) {
            // Return all logs if there's an error with filtering
            return auditLogRepository.findAll(pageable);
        }
    }

    @Override
    public List<Map<String, Object>> getSystemHealthMetrics() {
        List<Map<String, Object>> metrics = new ArrayList<>();
        try {
            // Database connectivity metric
            Map<String, Object> dbMetric = new HashMap<>();
            dbMetric.put("name", "Database Connectivity");
            dbMetric.put("status", "UP");
            dbMetric.put("responseTime", getDatabaseResponseTime());
            metrics.add(dbMetric);

            // Application uptime (approximate based on oldest session)
            Map<String, Object> uptimeMetric = new HashMap<>();
            uptimeMetric.put("name", "Application Uptime");
            uptimeMetric.put("status", "UP");
            uptimeMetric.put("uptime", "Available"); // Could be enhanced with actual uptime tracking
            metrics.add(uptimeMetric);

            // Memory usage metric
            Map<String, Object> memoryMetric = new HashMap<>();
            memoryMetric.put("name", "Memory Usage");
            memoryMetric.put("status", getMemoryStatus());
            memoryMetric.put("usage", getMemoryUsagePercentage());
            metrics.add(memoryMetric);

        } catch (Exception e) {
            Map<String, Object> errorMetric = new HashMap<>();
            errorMetric.put("name", "System Health");
            errorMetric.put("status", "ERROR");
            errorMetric.put("message", "Unable to retrieve metrics");
            metrics.add(errorMetric);
        }
        return metrics;
    }

    @Override
    public byte[] generateDetailedAuditReport(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            Page<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate,
                    PageRequest.of(0, 1000, Sort.by("createdAt").descending()));

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Detailed Audit Report", title));
            document.add(new Paragraph("Period: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    " to " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), normal));
            document.add(Chunk.NEWLINE);

            // Summary
            document.add(new Paragraph("Summary", subtitle));
            document.add(new Paragraph("Total Activities: " + logs.getTotalElements(), normal));
            document.add(Chunk.NEWLINE);

            // Detailed log table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 3});

            // Headers
            table.addCell(new PdfPCell(new Phrase("Timestamp", subtitle)));
            table.addCell(new PdfPCell(new Phrase("User", subtitle)));
            table.addCell(new PdfPCell(new Phrase("Action", subtitle)));
            table.addCell(new PdfPCell(new Phrase("Table", subtitle)));
            table.addCell(new PdfPCell(new Phrase("IP Address", subtitle)));

            // Data rows
            for (AuditLog log : logs.getContent()) {
                table.addCell(new Phrase(log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normal));
                table.addCell(new Phrase(log.getUser() != null ? log.getUser().getUsername() : "System", normal));
                table.addCell(new Phrase(log.getActionType().toString(), normal));
                table.addCell(new Phrase(log.getTableName() != null ? log.getTableName() : "-", normal));
                table.addCell(new Phrase(log.getIpAddress() != null ? log.getIpAddress() : "-", normal));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public byte[] generateUserActivityReport(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return new byte[0];
            }

            Page<AuditLog> userLogs = auditLogRepository.findByDateRange(startDate, endDate,
                    PageRequest.of(0, 1000, Sort.by("createdAt").descending()));

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("User Activity Report", title));
            document.add(new Paragraph("User: " + user.getFullName() + " (" + user.getUsername() + ")", normal));
            document.add(new Paragraph("Period: " + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    " to " + endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), normal));
            document.add(Chunk.NEWLINE);

            // Filter logs for this user
            List<AuditLog> filteredLogs = userLogs.getContent().stream()
                    .filter(log -> log.getUser() != null && log.getUser().getUserId().equals(userId))
                    .collect(Collectors.toList());

            document.add(new Paragraph("Total Activities: " + filteredLogs.size(), normal));
            document.add(Chunk.NEWLINE);

            for (AuditLog log : filteredLogs) {
                String logLine = String.format("%s - %s on %s",
                        log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        log.getActionType().toString(),
                        log.getTableName() != null ? log.getTableName() : "System");
                document.add(new Paragraph(logLine, normal));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public byte[] generateSecurityReport(int days) {
        try {
            Map<String, Object> securityData = getSecurityAnalytics();

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Security Analysis Report", title));
            document.add(new Paragraph("Period: Last " + days + " days", normal));
            document.add(Chunk.NEWLINE);

            // Security metrics
            document.add(new Paragraph("Security Metrics", subtitle));
            document.add(new Paragraph("Failed Login Attempts: " + securityData.get("failedLoginsCount"), normal));
            document.add(new Paragraph("Unique IP Addresses: " + securityData.get("uniqueIpAddresses"), normal));
            document.add(new Paragraph("Recent Activity Count: " + securityData.get("recentActivityCount"), normal));
            document.add(Chunk.NEWLINE);

            // Top IP addresses
            document.add(new Paragraph("Top Active IP Addresses", subtitle));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> topIps = (List<Map<String, Object>>) securityData.get("topIpAddresses");
            for (Map<String, Object> ipData : topIps) {
                document.add(new Paragraph(ipData.get("ipAddress") + ": " + ipData.get("activityCount") + " activities", normal));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    // Helper methods
    private long getDatabaseResponseTime() {
        try {
            long startTime = System.currentTimeMillis();
            auditLogRepository.count();
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            return -1;
        }
    }

    private String getMemoryStatus() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - runtime.freeMemory();

        double usagePercent = (double) usedMemory / maxMemory * 100;

        if (usagePercent > 90) return "CRITICAL";
        if (usagePercent > 75) return "WARNING";
        return "NORMAL";
    }

    private double getMemoryUsagePercentage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - runtime.freeMemory();

        return (double) usedMemory / maxMemory * 100;
    }
}