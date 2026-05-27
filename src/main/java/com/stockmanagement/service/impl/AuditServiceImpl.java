package com.stockmanagement.service.impl;

import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.AuditLog;
import com.stockmanagement.entity.User;
import com.stockmanagement.repository.AuditLogRepository;
import com.stockmanagement.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public void logAction(User user, ActionType actionType, String tableName, Long recordId,
                          String oldValues, String newValues) {

        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = getUserAgent(request);

        logAction(user, actionType, tableName, recordId, oldValues, newValues, ipAddress, userAgent);
    }

    @Override
    public void logAction(User user, ActionType actionType, String tableName, Long recordId,
                          String oldValues, String newValues, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog(user, actionType, tableName, recordId,
                    oldValues, newValues, ipAddress, userAgent);
            auditLogRepository.save(auditLog);

            logger.info("Audit log created: User={}, Action={}, Table={}, RecordId={}",
                    user != null ? user.getUsername() : "SYSTEM", actionType, tableName, recordId);
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    @Override
    public void logLogin(User user, String ipAddress, String userAgent) {
        logAction(user, ActionType.LOGIN, "users", user.getUserId(),
                null, "User logged in", ipAddress, userAgent);
    }

    @Override
    public void logLogout(User user, String ipAddress, String userAgent) {
        logAction(user, ActionType.LOGOUT, "users", user.getUserId(),
                null, "User logged out", ipAddress, userAgent);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }
}