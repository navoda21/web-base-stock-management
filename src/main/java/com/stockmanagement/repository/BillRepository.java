package com.stockmanagement.repository;

import com.stockmanagement.entity.Bill;
import com.stockmanagement.entity.BillStatus;
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
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByCustomerId(Long customerId);

    List<Bill> findByBillDateBetween(LocalDateTime start, LocalDateTime end);

    List<Bill> findByStatus(BillStatus status);

    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.billDate BETWEEN :startDate AND :endDate")
    Double getTotalSalesInRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.billDate, SUM(b.totalAmount) FROM Bill b " +
            "WHERE b.billDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', b.billDate) " +
            "ORDER BY FUNCTION('DATE', b.billDate)")
    List<Object[]> getDailySales(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.billDate >= :date")
    long countBillsAfterDate(@Param("date") LocalDateTime date);

    @Query("SELECT b FROM Bill b WHERE b.status = :status")
    List<Bill> findAllByStatus(@Param("status") BillStatus status);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.status = :status")
    long countByStatus(@Param("status") BillStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Bill b WHERE b.id = :id")
    void hardDeleteById(@Param("id") Long id);
}


