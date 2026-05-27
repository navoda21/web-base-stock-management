package com.stockmanagement.security;

import com.stockmanagement.entity.User;
import com.stockmanagement.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

@Component
public class AuthenticationEventsListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventsListener.class);

    @Autowired
    private UserSessionService userSessionService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            Object principal = event.getAuthentication().getPrincipal();
            if (principal instanceof CustomUserPrincipal) {
                CustomUserPrincipal cup = (CustomUserPrincipal) principal;
                User user = cup.getUser();

                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs == null) {
                    logger.warn("AuthenticationSuccessEvent received but no ServletRequestAttributes available");
                    return;
                }

                HttpServletRequest request = attrs.getRequest();
                if (request == null) {
                    logger.warn("No HttpServletRequest available for AuthenticationSuccessEvent");
                    return;
                }

                HttpSession session = request.getSession(false);
                if (session == null) {
                    session = request.getSession(true);
                }

                String sessionId = session.getId();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
                String ua = request.getHeader("User-Agent");

                userSessionService.createSession(sessionId, user, ip, ua, LocalDateTime.now().plusMinutes(30));
                logger.info("Session persisted via AuthenticationSuccessEvent for user={}, sessionId={}", user.getUsername(), sessionId);
            }
        } catch (Exception e) {
            logger.error("Failed to persist session from AuthenticationSuccessEvent: {}", e.getMessage(), e);
        }
    }
}
