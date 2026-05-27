package com.stockmanagement.service;

import com.stockmanagement.entity.Item;
import com.stockmanagement.entity.Supplier;
import com.stockmanagement.entity.SupplierItem;
import com.stockmanagement.repository.ItemRepository;
import com.stockmanagement.repository.SupplierItemRepository;
import com.stockmanagement.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierItemService {

    @Autowired
    private SupplierItemRepository supplierItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * Add a new supplier-item relationship
     */
    public SupplierItem addSupplierItem(Long supplierId, Long itemId, Integer quantity, BigDecimal supplyPrice) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + supplierId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + itemId));

        // Check if relationship already exists
        Optional<SupplierItem> existing = supplierItemRepository.findBySupplierIdAndItemId(supplierId, itemId);
        if (existing.isPresent()) {
            throw new RuntimeException("This supplier already provides this item. Please update the existing relationship.");
        }

        SupplierItem supplierItem = new SupplierItem(supplier, item, quantity, supplyPrice);
        return supplierItemRepository.save(supplierItem);
    }

    /**
     * Update an existing supplier-item relationship
     */
    public SupplierItem updateSupplierItem(Long id, Integer quantity, BigDecimal supplyPrice,
                                           Boolean isPrimary, Integer leadTimeDays,
                                           Integer minOrderQuantity, String notes) {
        SupplierItem supplierItem = supplierItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupplierItem not found with ID: " + id));

        if (quantity != null) {
            supplierItem.setSuppliedQuantity(quantity);
        }
        if (supplyPrice != null) {
            supplierItem.setSupplyPrice(supplyPrice);
        }
        if (isPrimary != null) {
            // If setting as primary, unset other primary suppliers for this item
            if (isPrimary) {
                List<SupplierItem> otherSuppliers = supplierItemRepository.findByItemId(supplierItem.getItem().getId());
                otherSuppliers.forEach(si -> {
                    if (!si.getId().equals(id) && si.getIsPrimarySupplier()) {
                        si.setIsPrimarySupplier(false);
                        supplierItemRepository.save(si);
                    }
                });
            }
            supplierItem.setIsPrimarySupplier(isPrimary);
        }
        if (leadTimeDays != null) {
            supplierItem.setLeadTimeDays(leadTimeDays);
        }
        if (minOrderQuantity != null) {
            supplierItem.setMinOrderQuantity(minOrderQuantity);
        }
        if (notes != null) {
            supplierItem.setNotes(notes);
        }

        return supplierItemRepository.save(supplierItem);
    }

    /**
     * Remove a supplier-item relationship
     */
    public void removeSupplierItem(Long id) {
        supplierItemRepository.deleteById(id);
    }

    /**
     * Get all items supplied by a specific supplier
     */
    public List<SupplierItem> getItemsBySupplier(Long supplierId) {
        return supplierItemRepository.findBySupplierId(supplierId);
    }

    /**
     * Get all suppliers for a specific item
     */
    public List<SupplierItem> getSuppliersByItem(Long itemId) {
        return supplierItemRepository.findByItemId(itemId);
    }

    /**
     * Get a specific supplier-item relationship
     */
    public Optional<SupplierItem> getSupplierItem(Long supplierId, Long itemId) {
        return supplierItemRepository.findBySupplierIdAndItemId(supplierId, itemId);
    }

    /**
     * Get the primary supplier for an item
     */
    public Optional<SupplierItem> getPrimarySupplierForItem(Long itemId) {
        return supplierItemRepository.findPrimarySupplierForItem(itemId);
    }

    /**
     * Get all supplier-item relationships
     */
    public List<SupplierItem> getAllSupplierItems() {
        return supplierItemRepository.findAll();
    }

    /**
     * Get a supplier-item by ID
     */
    public Optional<SupplierItem> getSupplierItemById(Long id) {
        return supplierItemRepository.findById(id);
    }

    /**
     * Count suppliers for an item
     */
    public Long countSuppliersForItem(Long itemId) {
        return supplierItemRepository.countByItemId(itemId);
    }

    /**
     * Count items for a supplier
     */
    public Long countItemsForSupplier(Long supplierId) {
        return supplierItemRepository.countBySupplierId(supplierId);
    }

    /**
     * Check if a supplier provides an item
     */
    public boolean doesSupplierProvideItem(Long supplierId, Long itemId) {
        return supplierItemRepository.existsBySupplierIdAndItemId(supplierId, itemId);
    }
}
