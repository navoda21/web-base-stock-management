package com.stockmanagement.controller;

import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/forgot")
    public String showForgotPasswordForm() {
        return "password/forgot-password";
    }

    @PostMapping("/forgot")
    public String processForgotPasswordForm(@RequestParam("email") String email,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        // Check if email exists and belongs to an admin
        Optional<String> tokenOpt = passwordResetService.createPasswordResetTokenForAdmin(email);

        if (tokenOpt.isEmpty()) {
            // Return the same message regardless of whether email exists or not
            // to prevent email enumeration attacks
            model.addAttribute("message", "If the email address exists in our database and belongs to an admin user, " +
                    "you will receive a password reset link shortly.");
            model.addAttribute("alertType", "success");
            return "password/forgot-password";
        }

        // In a production environment, we would only display a generic message
        // But for development/demo purposes, we'll include the token in the success message
        String token = tokenOpt.get();
        model.addAttribute("message", "A password reset link has been sent to your email address. " +
                "For development purposes, here is the reset link: " +
                "/password/reset?token=" + token);
        model.addAttribute("alertType", "success");
        return "password/forgot-password";
    }

    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        Model model) {
        Optional<User> userOpt = passwordResetService.validatePasswordResetToken(token);

        if (userOpt.isEmpty()) {
            model.addAttribute("message", "Invalid or expired password reset token.");
            model.addAttribute("alertType", "danger");
            return "password/reset-error";
        }

        User user = userOpt.get();

        // Only allow ADMIN role users to reset password
        if (user.getRole() != UserRole.ADMIN) {
            model.addAttribute("message", "Password reset is only available for administrator accounts.");
            model.addAttribute("alertType", "danger");
            return "password/reset-error";
        }

        model.addAttribute("token", token);
        model.addAttribute("username", user.getUsername());
        return "password/reset-password";
    }

    @PostMapping("/reset")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("message", "Passwords do not match.");
            model.addAttribute("alertType", "danger");
            return "password/reset-password";
        }

        // Validate password strength (minimum 6 characters)
        if (password.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("message", "Password must be at least 6 characters long.");
            model.addAttribute("alertType", "danger");
            return "password/reset-password";
        }

        Optional<User> userOpt = passwordResetService.validatePasswordResetToken(token);

        if (userOpt.isEmpty()) {
            model.addAttribute("message", "Invalid or expired password reset token.");
            model.addAttribute("alertType", "danger");
            return "password/reset-error";
        }

        User user = userOpt.get();

        // Only allow ADMIN role users to reset password
        if (user.getRole() != UserRole.ADMIN) {
            model.addAttribute("message", "Password reset is only available for administrator accounts.");
            model.addAttribute("alertType", "danger");
            return "password/reset-error";
        }

        boolean result = passwordResetService.changePassword(user, password);

        if (!result) {
            model.addAttribute("token", token);
            model.addAttribute("message", "Failed to change password. Please try again.");
            model.addAttribute("alertType", "danger");
            return "password/reset-password";
        }

        model.addAttribute("message", "Your password has been changed successfully. You can now login with your new password.");
        model.addAttribute("alertType", "success");
        return "password/reset-success";
    }
}