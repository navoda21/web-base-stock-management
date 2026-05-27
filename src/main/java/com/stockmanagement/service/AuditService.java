// AuditService.java
package com.stockmanagement.service;

import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.User;

public interface AuditService {
    void logAction(User user, ActionType actionType, String tableName, Long recordId,
                   String oldValues, String newValues);

    void logAction(User user, ActionType actionType, String tableName, Long recordId,
                   String oldValues, String newValues, String ipAddress, String userAgent);

    void logLogin(User user, String ipAddress, String userAgent);

    void logLogout(User user, String ipAddress, String userAgent);
}