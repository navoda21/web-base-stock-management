package com.stockmanagement.service;

import com.stockmanagement.entity.User;

import java.time.LocalDateTime;

public interface UserSessionService {
    void createSession(String sessionId, User user, String ipAddress, String userAgent, LocalDateTime expiresAt);
}
