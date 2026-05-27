package com.stockmanagement.service;

import com.stockmanagement.entity.UserRole;
import com.stockmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetupService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if the system has at least one admin user
     *
     * @return true if admin exists, false otherwise
     */
    public boolean hasAdminUser() {
        return userRepository.findByRole(UserRole.ADMIN).stream().findAny().isPresent();
    }

    /**
     * Check if the system needs initial setup
     *
     * @return true if setup is required (no admin user exists)
     */
    public boolean requiresSetup() {
        return !hasAdminUser();
    }

    /**
     * Get the total count of users in the system
     *
     * @return user count
     */
    public long getUserCount() {
        return userRepository.count();
    }
}
