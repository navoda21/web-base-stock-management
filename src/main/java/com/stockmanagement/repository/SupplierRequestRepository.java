package com.stockmanagement.repository;

import com.stockmanagement.entity.SupplierRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRequestRepository extends JpaRepository<SupplierRequest, Long> {

    /**
     * Find all supplier requests by status
     */
    List<SupplierRequest> findByStatus(String status);

    /**
     * Find all supplier requests for a specific item
     */
    List<SupplierRequest> findByItemId(Long itemId);

    /**
     * Find all pending requests ordered by request date
     */
    List<SupplierRequest> findByStatusOrderByRequestedAtDesc(String status);
}
