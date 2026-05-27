package com.stockmanagement.service;

import com.stockmanagement.entity.PasswordResetToken;
import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.repository.PasswordResetTokenRepository;
import com.stockmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Create a password reset token for the user with the given email if they are an ADMIN
     *
     * @param email The email address of the user
     * @return Optional containing the token if successful, empty if user not found or not an admin
     */
    @Transactional
    public Optional<String> createPasswordResetTokenForAdmin(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty() || userOpt.get().getRole() != UserRole.ADMIN) {
            // Return empty if user not found or not an admin
            return Optional.empty();
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        // Check if token already exists for user
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            // Update existing token
            existingToken.get().updateToken(token);
            tokenRepository.save(existingToken.get());
        } else {
            // Create new token
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            tokenRepository.save(resetToken);
        }

        // Try to send email
        try {
            emailService.sendPasswordResetEmail(user, token);
            logger.info("Password reset email process initiated for user: {}", user.getUsername());
        } catch (Exception e) {
            // Log error but don't fail the operation
            logger.error("Error sending password reset email to {}: {}", user.getEmail(), e.getMessage());
            logger.debug("Detailed error", e);
        }

        return Optional.of(token);
    }

    /**
     * Validate a password reset token
     *
     * @param token The token to validate
     * @return Optional containing the user if token is valid, empty if not
     */
    public Optional<User> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Check if token is expired
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return Optional.empty();
        }

        // Check if user is an admin
        User user = resetToken.getUser();
        if (user.getRole() != UserRole.ADMIN) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /**
     * Change the password for a user
     *
     * @param user        The user to change the password for
     * @param newPassword The new password
     * @return true if password was changed, false otherwise
     */
    @Transactional
    public boolean changePassword(User user, String newPassword) {
        if (user == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }

        // Only allow admin users to reset password with this method
        if (user.getRole() != UserRole.ADMIN) {
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete all tokens for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        return true;
    }

    /**
     * Clean up expired tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllExpiredTokens(new Date());
    }
}