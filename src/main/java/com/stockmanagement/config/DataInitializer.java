package com.stockmanagement.config;

import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data Initializer - Creates default admin user if no admin exists
 * This runs automatically when the application starts
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    /**
     * Creates a default admin user if no admin exists in the system
     */
    private void initializeAdminUser() {
        try {
            // Check if any admin user exists
            if (userRepository.findByRole(UserRole.ADMIN).isEmpty()) {
                logger.info("No admin user found. Creating default admin user...");

                // Create default admin user
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPasswordHash(passwordEncoder.encode("admin123"));
                adminUser.setEmail("admin@stockmanagement.com");
                adminUser.setFirstName("System");
                adminUser.setLastName("Administrator");
                adminUser.setRole(UserRole.ADMIN);
                adminUser.setActive(true);
                adminUser.setCreatedAt(LocalDateTime.now());
                adminUser.setUpdatedAt(LocalDateTime.now());

                userRepository.save(adminUser);

                logger.info("========================================");
                logger.info("DEFAULT ADMIN USER CREATED SUCCESSFULLY");
                logger.info("========================================");
                logger.info("Username: admin");
                logger.info("Password: admin123");
                logger.info("========================================");
                logger.warn("IMPORTANT: Change the default password immediately after first login!");
                logger.info("========================================");

            } else {
                logger.info("Admin user already exists. Skipping initialization.");
            }
        } catch (Exception e) {
            logger.error("Error during admin user initialization: " + e.getMessage(), e);
        }
    }
}
