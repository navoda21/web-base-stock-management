package com.stockmanagement.config;

import com.stockmanagement.service.SetupService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to redirect all requests to the setup page if no admin user exists.
 * This ensures that the system cannot be accessed without first creating an administrator.
 * <p>
 * NOTE: This filter is currently DISABLED because DataInitializer automatically creates
 * a default admin user on system startup. If you want to enforce manual setup instead,
 * uncomment the @Component annotation below.
 */
//@Component  // Commented out - DataInitializer creates admin automatically
@Order(1) // Execute this filter first
public class SetupFilter implements Filter {

    @Autowired
    private SetupService setupService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // Allow access to static resources and setup page itself
        if (shouldAllowAccess(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if setup is required
        if (setupService.requiresSetup()) {
            // Redirect to setup page if not already there
            if (!requestURI.startsWith("/setup")) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/setup");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Determine if the request should be allowed without setup check
     */
    private boolean shouldAllowAccess(String requestURI) {
        return requestURI.startsWith("/setup") ||
                requestURI.startsWith("/css") ||
                requestURI.startsWith("/js") ||
                requestURI.startsWith("/images") ||
                requestURI.startsWith("/favicon") ||
                requestURI.startsWith("/webjars") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".png") ||
                requestURI.endsWith(".jpg") ||
                requestURI.endsWith(".jpeg") ||
                requestURI.endsWith(".gif") ||
                requestURI.endsWith(".ico");
    }
}
