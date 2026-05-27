package com.stockmanagement.controller;

import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.AuditLog;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @GetMapping
    public String reportsDashboard(Model model) {
        // Get summary statistics
        Map<String, Object> summaryStats = reportsService.getSummaryStatistics();
        model.addAttribute("summaryStats", summaryStats);

        // Get recent activity
        List<AuditLog> recentActivity = reportsService.getRecentActivity(10);
        model.addAttribute("recentActivity", recentActivity);

        // Get action type counts
        Map<ActionType, Long> actionCounts = reportsService.getActionTypeCounts();
        model.addAttribute("actionCounts", actionCounts);

        return "admin/reports";
    }

    @GetMapping("/export/pdf")
    public void exportPdf(HttpServletResponse response) {
        try {
            byte[] pdf = reportsService.generateSummaryPdf();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=reports-summary.pdf");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            // simple error log
            System.err.println("Error generating PDF: " + e.getMessage());
        }
    }

    // Audit logs view removed; logs functionality is no longer exposed via a dedicated UI

    @GetMapping("/user-activity")
    public String userActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> userActivity = reportsService.getUserActivity(userId, pageable);

        model.addAttribute("userActivity", userActivity);
        model.addAttribute("selectedUserId", userId);

        return "admin/user-activity";
    }


    @GetMapping("/api/summary")
    @ResponseBody
    public Map<String, Object> getSummaryStats() {
        return reportsService.getSummaryStatistics();
    }

    @GetMapping("/api/activity-chart")
    @ResponseBody
    public Map<String, Object> getActivityChartData(
            @RequestParam(defaultValue = "7") int days) {
        return reportsService.getActivityChartData(days);
    }

    @GetMapping("/api/action-distribution")
    @ResponseBody
    public Map<ActionType, Long> getActionDistribution() {
        return reportsService.getActionTypeCounts();
    }

    // Enhanced Analytics Endpoints

    @GetMapping("/analytics")
    public String analyticsView(Model model) {
        // Get all analytics data for the analytics dashboard with error handling
        try {
            model.addAttribute("userStats", reportsService.getUserStatistics());
        } catch (Exception e) {
            Map<String, Object> defaultUserStats = new HashMap<>();
            defaultUserStats.put("totalUsers", 0);
            defaultUserStats.put("activeUsers", 0);
            defaultUserStats.put("inactiveUsers", 0);
            defaultUserStats.put("newUsersLastMonth", 0);
            model.addAttribute("userStats", defaultUserStats);
        }

        try {
            model.addAttribute("securityAnalytics", reportsService.getSecurityAnalytics());
        } catch (Exception e) {
            Map<String, Object> defaultSecurity = new HashMap<>();
            defaultSecurity.put("failedLoginsCount", 0);
            defaultSecurity.put("uniqueIpAddresses", 0);
            defaultSecurity.put("recentActivityCount", 0);
            model.addAttribute("securityAnalytics", defaultSecurity);
        }

        try {
            model.addAttribute("performanceMetrics", reportsService.getPerformanceMetrics());
        } catch (Exception e) {
            Map<String, Object> defaultPerformance = new HashMap<>();
            defaultPerformance.put("hourlyActivity", 0);
            defaultPerformance.put("dailyActivity", 0);
            defaultPerformance.put("activeSessions", 0);
            model.addAttribute("performanceMetrics", defaultPerformance);
        }

        try {
            model.addAttribute("topActiveUsers", reportsService.getTopActiveUsers(10));
        } catch (Exception e) {
            model.addAttribute("topActiveUsers", java.util.Collections.emptyList());
        }

        try {
            model.addAttribute("userDistribution", reportsService.getUserDistributionByRole());
        } catch (Exception e) {
            model.addAttribute("userDistribution", new HashMap<>());
        }

        try {
            model.addAttribute("systemHealth", reportsService.getSystemHealthMetrics());
        } catch (Exception e) {
            model.addAttribute("systemHealth", java.util.Collections.emptyList());
        }

        return "admin/analytics";
    }

    @GetMapping("/api/user-statistics")
    @ResponseBody
    public Map<String, Object> getUserStatistics() {
        return reportsService.getUserStatistics();
    }

    @GetMapping("/api/security-analytics")
    @ResponseBody
    public Map<String, Object> getSecurityAnalytics() {
        return reportsService.getSecurityAnalytics();
    }

    @GetMapping("/api/performance-metrics")
    @ResponseBody
    public Map<String, Object> getPerformanceMetrics() {
        return reportsService.getPerformanceMetrics();
    }

    @GetMapping("/api/top-active-users")
    @ResponseBody
    public List<Map<String, Object>> getTopActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        return reportsService.getTopActiveUsers(limit);
    }

    @GetMapping("/api/activity-by-hour")
    @ResponseBody
    public Map<String, Long> getActivityByHour() {
        return reportsService.getActivityByHour();
    }

    @GetMapping("/api/activity-by-day")
    @ResponseBody
    public Map<String, Long> getActivityByDay() {
        return reportsService.getActivityByDay();
    }

    @GetMapping("/api/failed-login-attempts")
    @ResponseBody
    public List<Map<String, Object>> getFailedLoginAttempts(
            @RequestParam(defaultValue = "7") int days) {
        return reportsService.getFailedLoginAttempts(days);
    }

    @GetMapping("/api/user-distribution")
    @ResponseBody
    public Map<UserRole, Long> getUserDistributionByRole() {
        return reportsService.getUserDistributionByRole();
    }

    @GetMapping("/api/system-health")
    @ResponseBody
    public List<Map<String, Object>> getSystemHealthMetrics() {
        return reportsService.getSystemHealthMetrics();
    }

    @GetMapping("/api/filtered-audit-logs")
    @ResponseBody
    public Page<AuditLog> getFilteredAuditLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Map<String, Object> filters = new HashMap<>();
        if (actionType != null && !actionType.isEmpty()) {
            filters.put("actionType", actionType);
        }
        if (tableName != null && !tableName.isEmpty()) {
            filters.put("tableName", tableName);
        }
        if (startDate != null) {
            filters.put("startDate", startDate);
        }
        if (endDate != null) {
            filters.put("endDate", endDate);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reportsService.getFilteredAuditLogs(filters, pageable);
    }

    // Enhanced PDF Export Endpoints

    @GetMapping("/export/detailed-audit-report")
    public ResponseEntity<byte[]> exportDetailedAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        byte[] pdf = reportsService.generateDetailedAuditReport(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "detailed-audit-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/export/user-activity-report/{userId}")
    public ResponseEntity<byte[]> exportUserActivityReport(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        byte[] pdf = reportsService.generateUserActivityReport(userId, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "user-activity-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/export/security-report")
    public ResponseEntity<byte[]> exportSecurityReport(
            @RequestParam(defaultValue = "30") int days) {

        byte[] pdf = reportsService.generateSecurityReport(days);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "security-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }


}