package com.stockmanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            logger.info("User {} already authenticated, redirecting to dashboard", auth.getName());
            return "redirect:/admin/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        // Check if user is already authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            logger.info("User {} already authenticated, redirecting to dashboard", auth.getName());
            return "redirect:/admin/dashboard";
        }

        if (error != null) {
            logger.warn("Login attempt failed");
        }

        if (logout != null) {
            logger.info("User logged out successfully");
        }

        return "login";
    }

    @GetMapping("/coming-soon")
    public String comingSoonPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Get user details from authentication
            if (auth.getPrincipal() instanceof com.stockmanagement.security.CustomUserPrincipal) {
                com.stockmanagement.security.CustomUserPrincipal userPrincipal =
                        (com.stockmanagement.security.CustomUserPrincipal) auth.getPrincipal();
                com.stockmanagement.entity.User user = userPrincipal.getUser();

                model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("userRole", user.getRole().toString());
            }
        }
        return "coming-soon";
    }

    @GetMapping("/access-denied")
    public String accessDeniedPage(Model model) {
        model.addAttribute("errorMessage", "You don't have permission to access this resource.");
        return "error/403";
    }
}