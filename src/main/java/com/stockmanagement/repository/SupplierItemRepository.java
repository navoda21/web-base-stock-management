package com.stockmanagement.repository;

import com.stockmanagement.entity.Item;
import com.stockmanagement.entity.Supplier;
import com.stockmanagement.entity.SupplierItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierItemRepository extends JpaRepository<SupplierItem, Long> {

    /**
     * Find all items supplied by a specific supplier
     */
    List<SupplierItem> findBySupplier(Supplier supplier);

    /**
     * Find all items supplied by a supplier ID
     */
    List<SupplierItem> findBySupplierId(Long supplierId);

    /**
     * Find all suppliers for a specific item
     */
    List<SupplierItem> findByItem(Item item);

    /**
     * Find all suppliers for an item ID
     */
    List<SupplierItem> findByItemId(Long itemId);

    /**
     * Find a specific supplier-item relationship
     */
    Optional<SupplierItem> findBySupplierIdAndItemId(Long supplierId, Long itemId);

    /**
     * Find the primary supplier for a specific item
     */
    @Query("SELECT si FROM SupplierItem si WHERE si.item.id = :itemId AND si.isPrimarySupplier = true")
    Optional<SupplierItem> findPrimarySupplierForItem(@Param("itemId") Long itemId);

    /**
     * Find all items supplied by a supplier with quantity greater than specified
     */
    @Query("SELECT si FROM SupplierItem si WHERE si.supplier.id = :supplierId AND si.suppliedQuantity > :minQuantity")
    List<SupplierItem> findBySupplierIdAndQuantityGreaterThan(@Param("supplierId") Long supplierId, @Param("minQuantity") Integer minQuantity);

    /**
     * Count how many suppliers provide a specific item
     */
    Long countByItemId(Long itemId);

    /**
     * Count how many items a supplier provides
     */
    Long countBySupplierId(Long supplierId);

    /**
     * Check if a supplier-item relationship exists
     */
    boolean existsBySupplierIdAndItemId(Long supplierId, Long itemId);
}
