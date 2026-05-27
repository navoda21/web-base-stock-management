package com.stockmanagement.observer;

import com.stockmanagement.entity.Item;

/**
 * Observer interface for stock change notifications
 * Observer Pattern - Observer Interface
 */
public interface StockObserver {

    /**
     * Called when stock levels change
     *
     * @param item        The item whose stock changed
     * @param oldQuantity Previous stock quantity
     * @param newQuantity New stock quantity
     */
    void onStockChange(Item item, int oldQuantity, int newQuantity);

    /**
     * Get observer name for logging and identification
     *
     * @return Observer name
     */
    String getObserverName();
}
