package com.stockmanagement.service.impl;

import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserSession;
import com.stockmanagement.repository.UserSessionRepository;
import com.stockmanagement.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserSessionServiceImpl implements UserSessionService {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionServiceImpl.class);

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createSession(String sessionId, User user, String ipAddress, String userAgent, LocalDateTime expiresAt) {
        try {
            UserSession userSession = new UserSession(sessionId, user, ipAddress, userAgent, expiresAt);
            userSessionRepository.saveAndFlush(userSession);
            logger.info("Created user session: sessionId={}, userId={}", sessionId, user != null ? user.getUserId() : null);
        } catch (Exception e) {
            logger.error("Failed to create user session for sessionId={}: {}", sessionId, e.getMessage(), e);
            // swallow exception to avoid breaking auth flow
        }
    }
}

