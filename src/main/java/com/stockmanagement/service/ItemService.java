package com.stockmanagement.service;

import com.stockmanagement.entity.Item;
import com.stockmanagement.observer.StockObserver;
import com.stockmanagement.observer.StockSubject;
import com.stockmanagement.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService implements StockSubject {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ViewItemService viewItemService;

    @Autowired
    private AddItemService addItemService;

    @Autowired
    private UpdateItemService updateItemService;

    @Autowired
    private DeleteItemService deleteItemService;

    // Observer Pattern: List of observers
    private final List<StockObserver> observers = new ArrayList<>();

    // ===== OBSERVER PATTERN METHODS =====

    @Override
    public void registerObserver(StockObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("✅ Registered observer: " + observer.getObserverName());
        }
    }

    @Override
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
        System.out.println("❌ Removed observer: " + observer.getObserverName());
    }

    @Override
    public void notifyObservers(Item item, int oldQuantity, int newQuantity) {
        System.out.println("\n🔔 Notifying " + observers.size() + " observers about stock change");
        System.out.println("   Item: " + item.getName() + " | Old: " + oldQuantity + " → New: " + newQuantity);
        System.out.println("───────────────────────────────────────────────────────────");

        for (StockObserver observer : observers) {
            try {
                observer.onStockChange(item, oldQuantity, newQuantity);
            } catch (Exception e) {
                System.err.println("❌ Error in observer " + observer.getObserverName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("───────────────────────────────────────────────────────────\n");
    }

    // ===== EXISTING METHODS =====

    /**
     * Get all items
     */
    public List<Item> getAllItems() {
        return viewItemService.getAllItems();
    }

    /**
     * Get item by ID
     */
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Get item by ID (backward compatibility)
     */
    public Optional<Item> getItemById(int id) {
        return getItemById((long) id);
    }

    /**
     * Add a new item
     */
    public Item addItem(Item item) {
        addItemService.addItem(item);
        return item;
    }

    /**
     * Update an existing item
     */
    public Item updateItem(Item item) {
        updateItemService.updateItem(item);
        return item;
    }

    /**
     * Delete an item
     */
    public void deleteItem(int id) {
        deleteItemService.deleteItem(id);
    }

    /**
     * Update stock quantity
     * REFACTORED: Now using Observer Pattern to notify all observers
     */
    public void updateStock(int itemId, int quantityChange) {
        Optional<Item> itemOpt = getItemById(itemId);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();

            // Store old quantity for observers
            int oldQuantity = item.getQuantity();

            // For purchases, quantityChange should be negative
            int newQuantity = item.getQuantity() - quantityChange;
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Insufficient stock for item: " + item.getName());
            }

            // Update the quantity
            item.setQuantity(newQuantity);
            updateItem(item);

            // 🔔 NOTIFY ALL OBSERVERS ABOUT THE CHANGE (Observer Pattern)
            notifyObservers(item, oldQuantity, newQuantity);

        } else {
            throw new IllegalArgumentException("Item not found with id: " + itemId);
        }
    }

    /**
     * Get all items with low stock (quantity <= threshold)
     */
    public List<Item> getLowStockItems(int threshold) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() <= threshold)
                .toList();
    }

    /**
     * Get all items with low stock (default threshold = 10)
     */
    public List<Item> getLowStockItems() {
        return getLowStockItems(10);
    }
}