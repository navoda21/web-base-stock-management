package com.stockmanagement.security;

import com.stockmanagement.entity.User;
// user session persistence delegated to UserSessionService
import com.stockmanagement.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessHandler.class);

    @Autowired
    private AuditService auditService;

    @Autowired
    private com.stockmanagement.service.UserSessionService userSessionService;

    public AuthenticationSuccessHandler() {
        // Default remains admin dashboard, but we'll route dynamically per role below
        setDefaultTargetUrl("/admin/dashboard");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        HttpSession session = request.getSession();

        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            // Log successful login
            auditService.logLogin(user, ipAddress, userAgent);

            // Create user session record in its own transaction
            userSessionService.createSession(
                    session.getId(),
                    user,
                    ipAddress,
                    userAgent,
                    LocalDateTime.now().plusMinutes(30)
            );

            logger.info("User {} logged in successfully from IP: {}", user.getUsername(), ipAddress);
        } catch (Exception e) {
            // Never break the login flow due to audit/session persistence issues
            logger.error("Post-login processing failed; proceeding with redirect. Cause: {}", e.getMessage(), e);
        }

        // Route by role to appropriate dashboard
        String targetUrl = "/admin/dashboard"; // Default fallback

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        boolean isStockManager = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_STOCK_MANAGER"));
        boolean isSalesStaff = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_SALES_STAFF"));
        boolean isHRStaff = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_HR_STAFF"));
        boolean isMarketingManager = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_MARKETING_MANAGER"));

        if (isAdmin) {
            targetUrl = "/admin/dashboard";
        } else if (isStockManager) {
            targetUrl = "/items/dashboard";
        } else if (isSalesStaff) {
            targetUrl = "/bills";
        } else if (isHRStaff) {
            targetUrl = "/staff";
        } else if (isMarketingManager) {
            targetUrl = "/promotions";
        }

        try {
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            logger.error("Redirect to {} failed: {}", targetUrl, e.getMessage(), e);
            // Fallback: ensure response is at least redirected to login page to avoid 500
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
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
}
