package com.stockmanagement.repository;

import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    List<User> findByRole(UserRole role);

    Page<User> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRole role);

    // Analytics queries
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserDistributionByRole();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    Long countNewUsers(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM User u " +
            "WHERE u.createdAt >= :since GROUP BY DATE(u.createdAt) ORDER BY date")
    List<Object[]> getUserRegistrationTrend(@Param("since") java.time.LocalDateTime since);

    // Clear self-referencing foreign keys before hard delete
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.createdBy = null WHERE u.createdBy = :user")
    void clearCreatedByReferences(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.updatedBy = null WHERE u.updatedBy = :user")
    void clearUpdatedByReferences(@Param("user") User user);
}