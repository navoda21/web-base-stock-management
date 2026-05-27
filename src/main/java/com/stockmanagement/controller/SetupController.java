package com.stockmanagement.controller;

import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.repository.UserRepository;
import com.stockmanagement.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/setup")
public class SetupController {

    @Autowired
    private SetupService setupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Show initial setup page if no admin exists
     */
    @GetMapping
    public String showSetupPage(Model model) {
        // Check if setup is already complete
        if (!setupService.requiresSetup()) {
            return "redirect:/login";
        }

        model.addAttribute("user", new User());
        return "setup/initial-setup";
    }

    /**
     * Create the first admin user
     */
    @PostMapping("/create-admin")
    public String createAdminUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Check if setup is already complete
        if (!setupService.requiresSetup()) {
            return "redirect:/login";
        }

        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username is required");
            model.addAttribute("user", new User());
            return "setup/initial-setup";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password is required");
            model.addAttribute("user", new User());
            return "setup/initial-setup";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long");
            model.addAttribute("user", new User());
            return "setup/initial-setup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("user", new User());
            return "setup/initial-setup";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            model.addAttribute("user", new User());
            return "setup/initial-setup";
        }

        try {
            // Create admin user
            User adminUser = new User();
            adminUser.setUsername(username.trim());
            adminUser.setPasswordHash(passwordEncoder.encode(password));
            adminUser.setFirstName(firstName != null ? firstName.trim() : "Admin");
            adminUser.setLastName(lastName != null ? lastName.trim() : "User");
            adminUser.setEmail(email.trim());
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setActive(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(adminUser);

            redirectAttributes.addFlashAttribute("setupSuccess", true);
            redirectAttributes.addFlashAttribute("message",
                    "Admin account created successfully! Please login with your credentials.");

            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Error creating admin user: " + e.getMessage());
            model.addAttribute("user", new User());
            return "setup/initial-setup";
        }
    }
}
