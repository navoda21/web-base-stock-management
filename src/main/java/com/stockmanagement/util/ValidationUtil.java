// ValidationUtil.java
package com.stockmanagement.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean isStrongPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.NONE;
        }

        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength++;

        return switch (strength) {
            case 0, 1 -> PasswordStrength.WEAK;
            case 2 -> PasswordStrength.FAIR;
            case 3 -> PasswordStrength.GOOD;
            case 4, 5 -> PasswordStrength.STRONG;
            default -> PasswordStrength.NONE;
        };
    }

    public enum PasswordStrength {
        NONE, WEAK, FAIR, GOOD, STRONG
    }
}