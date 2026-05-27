package com.stockmanagement.service;

import com.stockmanagement.dto.UserCreateDTO;
import com.stockmanagement.dto.UserResponseDTO;
import com.stockmanagement.dto.UserUpdateDTO;
import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO userCreateDTO, User currentUser);

    UserResponseDTO updateUser(Long userId, UserUpdateDTO userUpdateDTO, User currentUser);

    void deleteUser(Long userId, User currentUser);

    Optional<UserResponseDTO> getUserById(Long userId);

    Optional<UserResponseDTO> getUserByUsername(String username);

    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    Page<UserResponseDTO> searchUsers(String searchTerm, Pageable pageable);

    List<UserResponseDTO> getUsersByRole(UserRole role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void resetPassword(Long userId, String newPassword, User currentUser);

    void toggleUserStatus(Long userId, User currentUser);

    long getTotalActiveUsers();

    long getUserCountByRole(UserRole role);
}