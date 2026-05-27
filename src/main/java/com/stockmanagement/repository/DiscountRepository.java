package com.stockmanagement.repository;

import com.stockmanagement.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    List<Discount> findByActiveTrue();

    @Query("SELECT d FROM Discount d WHERE d.active = true AND d.startDate <= ?1 AND d.endDate >= ?1")
    List<Discount> findCurrentlyActiveDiscounts(LocalDate date);

    List<Discount> findByNameContainingIgnoreCase(String keyword);

    List<Discount> findByItemId(Long itemId);
}