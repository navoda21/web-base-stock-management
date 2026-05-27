package com.stockmanagement.repository;

import com.stockmanagement.entity.User;
import com.stockmanagement.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    Optional<UserSession> findBySessionIdAndIsActiveTrue(String sessionId);

    List<UserSession> findByUserAndIsActiveTrue(User user);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user = :user")
    void deactivateAllUserSessions(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :now")
    void deactivateExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user = :user AND s.isActive = true")
    long countActiveSessionsByUser(@Param("user") User user);
}