package com.stockmanagement.repository;

import com.stockmanagement.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByActiveTrue();

    @Query("SELECT p FROM Promotion p WHERE p.active = true AND p.startDate <= ?1 AND p.endDate >= ?1")
    List<Promotion> findCurrentlyActivePromotions(LocalDate date);

    List<Promotion> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Promotion p JOIN p.items i WHERE i.id = ?1")
    List<Promotion> findByItemId(Long itemId);
}