package com.stockmanagement.repository;

import com.stockmanagement.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmployeeId(String employeeId);

    Optional<Staff> findByEmail(String email);

    List<Staff> findByIsActiveTrue();

    List<Staff> findByDepartment(String department);

    List<Staff> findByRole(String role);

    @Query("SELECT s FROM Staff s WHERE s.isActive = true AND s.department = :department")
    List<Staff> findActiveStaffByDepartment(@Param("department") String department);

    List<Staff> findByHireDateAfterAndIsActiveTrue(LocalDate date);

    List<Staff> findByPerformanceRatingGreaterThanEqualAndIsActiveTrue(Double minRating);

    @Query("SELECT COUNT(s) FROM Staff s WHERE s.isActive = true")
    long countActiveStaff();

    @Query("SELECT s.department, COUNT(s) FROM Staff s WHERE s.isActive = true GROUP BY s.department")
    List<Object[]> countStaffByDepartment();

    // Hard delete method
    @Modifying
    @Transactional
    @Query("DELETE FROM Staff s WHERE s.id = :id")
    void hardDeleteById(@Param("id") Long id);
}