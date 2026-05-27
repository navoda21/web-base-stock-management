package com.stockmanagement.service.impl;

import com.stockmanagement.dto.UserCreateDTO;
import com.stockmanagement.dto.UserResponseDTO;
import com.stockmanagement.dto.UserUpdateDTO;
import com.stockmanagement.entity.ActionType;
import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import com.stockmanagement.exception.ResourceNotFoundException;
import com.stockmanagement.exception.DuplicateResourceException;
import com.stockmanagement.repository.UserRepository;
import com.stockmanagement.service.AuditService;
import com.stockmanagement.service.UserService;
import com.stockmanagement.util.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO, User currentUser) {
        logger.info("Creating new user with username: {}", userCreateDTO.getUsername());

        // Check for existing username
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + userCreateDTO.getUsername());
        }

        // Check for existing email
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + userCreateDTO.getEmail());
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(userCreateDTO.getUsername());
        newUser.setEmail(userCreateDTO.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(userCreateDTO.getPassword()));
        newUser.setFirstName(userCreateDTO.getFirstName());
        newUser.setLastName(userCreateDTO.getLastName());
        newUser.setRole(userCreateDTO.getRole());
        newUser.setActive(true);
        newUser.setCreatedBy(currentUser);

        User savedUser = userRepository.save(newUser);

        // Log audit trail
        auditService.logAction(currentUser, ActionType.CREATE, "users", savedUser.getUserId(),
                null, userMapper.toJson(savedUser));

        logger.info("Successfully created user with ID: {}", savedUser.getUserId());
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, UserUpdateDTO userUpdateDTO, User currentUser) {
        logger.info("Updating user with ID: {}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String oldValues = userMapper.toJson(existingUser);

        // Update fields
        existingUser.setEmail(userUpdateDTO.getEmail());
        existingUser.setFirstName(userUpdateDTO.getFirstName());
        existingUser.setLastName(userUpdateDTO.getLastName());
        existingUser.setRole(userUpdateDTO.getRole());
        existingUser.setUpdatedBy(currentUser);

        User updatedUser = userRepository.save(existingUser);

        // Log audit trail
        auditService.logAction(currentUser, ActionType.UPDATE, "users", updatedUser.getUserId(),
                oldValues, userMapper.toJson(updatedUser));

        logger.info("Successfully updated user with ID: {}", userId);
        return userMapper.toResponseDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long userId, User currentUser) {
        logger.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Prevent deletion of active users
        if (user.isActive()) {
            throw new IllegalStateException("Cannot delete active users. Please deactivate the user first before deletion.");
        }

        String oldValues = userMapper.toJson(user);

        // Clear self-referencing FKs to avoid constraint violations
        try {
            userRepository.clearCreatedByReferences(user);
            userRepository.clearUpdatedByReferences(user);
        } catch (Exception e) {
            logger.warn("Failed to clear createdBy/updatedBy references for user {}: {}", userId, e.getMessage());
        }

        // Hard delete
        userRepository.delete(user);

        // Log audit trail (record the fact of deletion; after delete we only keep old values)
        auditService.logAction(currentUser, ActionType.DELETE, "users", userId, oldValues, null);

        logger.info("Successfully hard deleted user with ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void resetPassword(Long userId, String newPassword, User currentUser) {
        logger.info("Resetting password for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(currentUser);
        userRepository.save(user);

        // Log audit trail (without exposing password) as valid JSON blobs
        auditService.logAction(currentUser, ActionType.UPDATE, "users", userId,
                "{\"password\":\"reset\"}", "{\"password\":\"updated\"}");

        logger.info("Successfully reset password for user with ID: {}", userId);
    }

    @Override
    public void toggleUserStatus(Long userId, User currentUser) {
        logger.info("Toggling status for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean oldStatus = user.isActive();
        user.setActive(!oldStatus);
        user.setUpdatedBy(currentUser);
        userRepository.save(user);

        // Log audit trail with valid JSON for JSON column
        auditService.logAction(currentUser, ActionType.UPDATE, "users", userId,
                "{\"active\":" + oldStatus + "}", "{\"active\":" + user.isActive() + "}");

        logger.info("Successfully toggled status for user with ID: {} to {}", userId, user.isActive());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalActiveUsers() {
        return userRepository.countActiveUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserCountByRole(UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }
}