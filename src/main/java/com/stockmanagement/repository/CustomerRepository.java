package com.stockmanagement.repository;

import com.stockmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    List<Customer> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);

    Optional<Customer> findByCustomerId(String customerId);

    List<Customer> findByIsActiveTrue();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.isActive = true")
    long countActiveCustomers();

    @Query("SELECT c FROM Customer c WHERE " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.phone LIKE CONCAT('%', :search, '%') " +
            "OR COALESCE(c.customerId, '') LIKE CONCAT('%', :search, '%'))")
    List<Customer> searchCustomers(@Param("search") String search);

    @Query("SELECT c.membershipLevel, COUNT(c) FROM Customer c GROUP BY c.membershipLevel")
    List<Object[]> countCustomersByMembershipLevel();

    // Hard delete method
    @Modifying
    @Transactional
    @Query("DELETE FROM Customer c WHERE c.id = :id")
    void hardDeleteById(@Param("id") Long id);
}

