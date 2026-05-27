package com.stockmanagement.service;

import com.stockmanagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private JavaMailSender mailSender;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${server.port:8080}")
    private String serverPort;

    // Default constructor to avoid initialization errors when mailSender is not configured
    public EmailService() {
    }

    @Autowired(required = false) // Make it optional
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a password reset email to the user
     *
     * @param user  The user to send the email to
     * @param token The reset token
     */
    public void sendPasswordResetEmail(User user, String token) {
        if (!mailEnabled) {
            logger.info("Email sending is disabled. Would have sent reset email to: {}", user.getEmail());
            return;
        }

        if (mailSender == null) {
            logger.error("JavaMailSender is not configured but email sending is enabled!");
            return;
        }

        // Validate email address format
        String email = user.getEmail();
        if (email == null || !email.contains("@") || !email.contains(".")) {
            logger.error("Invalid email address format: {}", email);
            throw new IllegalArgumentException("Invalid email address format");
        }

        String resetUrl = "http://localhost:" + serverPort + "/password/reset?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Stock Management System - Password Reset");
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "You have requested to reset your password. Please click on the link below to reset your password:\n\n" +
                resetUrl + "\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Regards,\nStock Management System");

        try {
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
            throw e; // Re-throw so the calling service knows it failed
        }
    }
}