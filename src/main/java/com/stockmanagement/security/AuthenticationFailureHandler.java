// AuthenticationFailureHandler.java
package com.stockmanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        String ipAddress = getClientIpAddress(request);

        if (exception instanceof BadCredentialsException) {
            logger.warn("Failed login attempt for username: {} from IP: {} - Invalid credentials",
                    username, ipAddress);
        } else if (exception instanceof DisabledException || exception instanceof UsernameNotFoundException) {
            logger.warn("Failed login attempt for username: {} from IP: {} - Account disabled/not found",
                    username, ipAddress);
        } else {
            logger.warn("Failed login attempt for username: {} from IP: {} - {}",
                    username, ipAddress, exception.getMessage());
        }

        setDefaultFailureUrl("/login?error=true");
        super.onAuthenticationFailure(request, response, exception);
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