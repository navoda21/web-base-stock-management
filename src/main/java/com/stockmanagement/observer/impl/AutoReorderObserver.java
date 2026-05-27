package com.stockmanagement.observer.impl;

import com.stockmanagement.entity.Item;
import com.stockmanagement.observer.StockObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Observer that triggers automatic reorder when stock falls below threshold
 * Observer Pattern - Concrete Observer
 */
@Component
public class AutoReorderObserver implements StockObserver {

    private static final Logger logger = LoggerFactory.getLogger(AutoReorderObserver.class);
    private static final int REORDER_POINT = 10;
    private static final int STANDARD_REORDER_QUANTITY = 50;

    @Override
    public void onStockChange(Item item, int oldQuantity, int newQuantity) {
        // Trigger automatic reorder when crossing threshold
        if (oldQuantity >= REORDER_POINT && newQuantity < REORDER_POINT) {
            triggerAutoReorder(item, newQuantity);
        }

        // Log when stock is restored
        if (oldQuantity < REORDER_POINT && newQuantity >= REORDER_POINT) {
            logStockRestored(item, newQuantity);
        }
    }

    /**
     * Trigger automatic reorder
     */
    private void triggerAutoReorder(Item item, int currentQuantity) {
        int reorderQuantity = calculateReorderQuantity(item, currentQuantity);

        logger.warn("🔄 AUTO-REORDER TRIGGERED");
        logger.warn("═══════════════════════════════════════════════════════════");
        logger.warn("Item:             {}", item.getName());
        logger.warn("Item ID:          {}", item.getId());
        logger.warn("Current Stock:    {} units", currentQuantity);
        logger.warn("Reorder Point:    {} units", REORDER_POINT);
        logger.warn("Order Quantity:   {} units", reorderQuantity);
        logger.warn("Unit Price:       ${}", item.getPrice());
        BigDecimal orderValue = item.getPrice().multiply(new BigDecimal(reorderQuantity));
        logger.warn("Order Value:      ${}", String.format("%.2f", orderValue));
        logger.warn("───────────────────────────────────────────────────────────");
        logger.warn("Status:           Purchase order generated");
        logger.warn("Priority:         AUTOMATIC");
        logger.warn("═══════════════════════════════════════════════════════════");

        // Generate purchase order
        generatePurchaseOrder(item, reorderQuantity);

        // Notify purchasing department
        notifyPurchasingDepartment(item, reorderQuantity);
    }

    /**
     * Calculate reorder quantity based on item characteristics
     */
    private int calculateReorderQuantity(Item item, int currentQuantity) {
        // In production: Use more sophisticated algorithm
        // - Consider historical sales velocity
        // - Account for lead time
        // - Apply Economic Order Quantity (EOQ) formula
        // - Check seasonal patterns

        // Simple calculation: Order enough to reach safe stock level
        int targetStock = 100; // Safe stock level
        int neededQuantity = targetStock - currentQuantity;

        // Round up to standard order quantity
        return Math.max(neededQuantity, STANDARD_REORDER_QUANTITY);
    }

    /**
     * Generate purchase order (simulated)
     */
    private void generatePurchaseOrder(Item item, int quantity) {
        logger.info("📄 Purchase Order Generated");
        logger.info("   PO Number:    PO-{}-{}", System.currentTimeMillis(), item.getId());
        logger.info("   Item:         {}", item.getName());
        logger.info("   Quantity:     {} units", quantity);
        BigDecimal totalValue = item.getPrice().multiply(new BigDecimal(quantity));
        logger.info("   Total Value:  ${}", String.format("%.2f", totalValue));
        logger.info("   Status:       PENDING APPROVAL");

        // In production: Create PurchaseOrder entity and save to database
        // PurchaseOrder po = new PurchaseOrder();
        // po.setItem(item);
        // po.setQuantity(quantity);
        // po.setStatus("PENDING");
        // po.setCreatedDate(LocalDateTime.now());
        // purchaseOrderService.save(po);
    }

    /**
     * Notify purchasing department
     */
    private void notifyPurchasingDepartment(Item item, int quantity) {
        logger.info("📧 Notification sent to purchasing@company.com");
        logger.info("   Subject: Auto-Reorder Alert - {}", item.getName());
        logger.info("   Message: Automatic purchase order generated for {} units of {}",
                quantity, item.getName());
        logger.info("   Action Required: Review and approve purchase order");

        // In production: Send actual email
        // emailService.sendEmail("purchasing@company.com", subject, body);
    }

    /**
     * Log when stock is restored above reorder point
     */
    private void logStockRestored(Item item, int newQuantity) {
        logger.info("✅ STOCK RESTORED ABOVE REORDER POINT");
        logger.info("   Item:         {}", item.getName());
        logger.info("   New Stock:    {} units", newQuantity);
        logger.info("   Reorder Point: {} units", REORDER_POINT);
        logger.info("   Status:       Normal stock level maintained");
    }

    @Override
    public String getObserverName() {
        return "AutoReorderObserver";
    }
}
