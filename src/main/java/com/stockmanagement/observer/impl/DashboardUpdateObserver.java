package com.stockmanagement.observer.impl;

import com.stockmanagement.entity.Item;
import com.stockmanagement.observer.StockObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Observer that updates real-time dashboard statistics
 * Observer Pattern - Concrete Observer
 */
@Component
public class DashboardUpdateObserver implements StockObserver {

    private static final Logger logger = LoggerFactory.getLogger(DashboardUpdateObserver.class);

    // Simulated dashboard statistics (in production, use cache or database)
    private static int lowStockItemsCount = 0;
    private static int outOfStockItemsCount = 0;
    private static double totalInventoryValue = 0.0;

    @Override
    public void onStockChange(Item item, int oldQuantity, int newQuantity) {
        // Update low stock count
        updateLowStockCount(oldQuantity, newQuantity);

        // Update out of stock count
        updateOutOfStockCount(oldQuantity, newQuantity);

        // Update inventory value
        updateInventoryValue(item, oldQuantity, newQuantity);

        // Log dashboard update
        logDashboardUpdate(item, newQuantity);

        // In production: Update cache, broadcast to WebSocket clients
        // broadcastToWebSocket(dashboardData);
    }

    /**
     * Update low stock items count
     */
    private void updateLowStockCount(int oldQuantity, int newQuantity) {
        // If item dropped below threshold
        if (oldQuantity >= 5 && newQuantity < 5 && newQuantity > 0) {
            lowStockItemsCount++;
        }
        // If item restocked above threshold
        else if (oldQuantity < 5 && newQuantity >= 5) {
            lowStockItemsCount--;
            if (lowStockItemsCount < 0) lowStockItemsCount = 0;
        }
    }

    /**
     * Update out of stock items count
     */
    private void updateOutOfStockCount(int oldQuantity, int newQuantity) {
        // Item just went out of stock
        if (oldQuantity > 0 && newQuantity == 0) {
            outOfStockItemsCount++;
        }
        // Item restocked
        else if (oldQuantity == 0 && newQuantity > 0) {
            outOfStockItemsCount--;
            if (outOfStockItemsCount < 0) outOfStockItemsCount = 0;
        }
    }

    /**
     * Update total inventory value
     */
    private void updateInventoryValue(Item item, int oldQuantity, int newQuantity) {
        BigDecimal oldValue = item.getPrice().multiply(new BigDecimal(oldQuantity));
        BigDecimal newValue = item.getPrice().multiply(new BigDecimal(newQuantity));
        BigDecimal valueDiff = newValue.subtract(oldValue);

        totalInventoryValue += valueDiff.doubleValue();

        logger.debug("💰 Inventory value changed by ${} (Total: ${})",
                String.format("%.2f", valueDiff),
                String.format("%.2f", totalInventoryValue));
    }

    /**
     * Log dashboard update
     */
    private void logDashboardUpdate(Item item, int newQuantity) {
        logger.info("📊 DASHBOARD UPDATE - Stock Changed");
        logger.info("   Item: {} (Qty: {})", item.getName(), newQuantity);
        logger.info("   ┌─────────────────────────────────────────");
        logger.info("   │ Low Stock Items:     {}", lowStockItemsCount);
        logger.info("   │ Out of Stock Items:  {}", outOfStockItemsCount);
        logger.info("   │ Total Inventory Value: ${}", String.format("%.2f", totalInventoryValue));
        logger.info("   └─────────────────────────────────────────");

        // In production: Send real-time update via WebSocket
        sendWebSocketUpdate(item, newQuantity);
    }

    /**
     * Send real-time update via WebSocket (simulated)
     */
    private void sendWebSocketUpdate(Item item, int newQuantity) {
        logger.debug("🔄 WebSocket update sent to connected clients");
        logger.debug("   Topic: /topic/stock-updates");
        logger.debug("   Payload: {{itemId: {}, itemName: '{}', newQuantity: {}}}",
                item.getId(), item.getName(), newQuantity);

        // In production:
        // messagingTemplate.convertAndSend("/topic/stock-updates", 
        //     new StockUpdateMessage(item.getId(), item.getName(), newQuantity));
    }

    /**
     * Get current dashboard statistics (for testing/debugging)
     */
    public static String getDashboardStats() {
        return String.format("Low Stock: %d | Out of Stock: %d | Total Value: $%.2f",
                lowStockItemsCount, outOfStockItemsCount, totalInventoryValue);
    }

    @Override
    public String getObserverName() {
        return "DashboardUpdateObserver";
    }
}
