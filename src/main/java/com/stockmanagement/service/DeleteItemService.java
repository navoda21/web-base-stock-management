package com.stockmanagement.service;

import com.stockmanagement.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteItemService {

    private final ItemRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public DeleteItemService(ItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void deleteItem(int id) {
        Long itemId = (long) id;

        // First, check if item exists
        if (!repository.existsById(itemId)) {
            throw new RuntimeException("Item not found with ID: " + id);
        }

        // Delete related records first to avoid foreign key constraint

        // 1. Delete from promotion_items (many-to-many relationship)
        entityManager.createNativeQuery(
                "DELETE FROM promotion_items WHERE item_id = :itemId"
        ).setParameter("itemId", itemId).executeUpdate();

        // 2. Delete from discounts (one-to-many relationship)
        entityManager.createNativeQuery(
                "DELETE FROM discounts WHERE item_id = :itemId"
        ).setParameter("itemId", itemId).executeUpdate();

        // 3. Delete from bill_items (foreign key constraint issue)
        entityManager.createNativeQuery(
                "DELETE FROM bill_items WHERE product_id = :itemId"
        ).setParameter("itemId", itemId).executeUpdate();

        // 4. Delete from supplier_requests (foreign key constraint)
        entityManager.createNativeQuery(
                "DELETE FROM supplier_requests WHERE item_id = :itemId"
        ).setParameter("itemId", itemId).executeUpdate();

        // Finally, delete the item itself
        repository.deleteById(itemId);
    }
}