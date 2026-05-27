package com.stockmanagement.observer;

import com.stockmanagement.entity.Item;

/**
 * Subject interface for stock change notifications
 * Observer Pattern - Subject Interface
 */
public interface StockSubject {

    /**
     * Register an observer to receive stock change notifications
     *
     * @param observer Observer to register
     */
    void registerObserver(StockObserver observer);

    /**
     * Remove an observer from receiving notifications
     *
     * @param observer Observer to remove
     */
    void removeObserver(StockObserver observer);

    /**
     * Notify all registered observers about stock changes
     *
     * @param item        The item that changed
     * @param oldQuantity Previous quantity
     * @param newQuantity New quantity
     */
    void notifyObservers(Item item, int oldQuantity, int newQuantity);
}
