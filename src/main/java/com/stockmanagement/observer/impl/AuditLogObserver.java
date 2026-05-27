package com.stockmanagement.observer.impl;

import com.stockmanagement.entity.Item;
import com.stockmanagement.observer.StockObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observer that logs all stock changes to audit trail
 * Observer Pattern - Concrete Observer
 */
@Component
public class AuditLogObserver implements StockObserver {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogObserver.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onStockChange(Item item, int oldQuantity, int newQuantity) {
        // Log every stock change to audit trail
        String timestamp = LocalDateTime.now().format(formatter);
        int change = newQuantity - oldQuantity;
        String changeType = change > 0 ? "INCREASE" : "DECREASE";

        logToAuditTrail(timestamp, item, oldQuantity, newQuantity, change, changeType);

        // Also log to database (in production)
        // auditService.logStockChange(item.getId(), oldQuantity, newQuantity, changeType);
    }

    /**
     * Log stock change to audit trail
     */
    private void logToAuditTrail(String timestamp, Item item, int oldQuantity,
                                 int newQuantity, int change, String changeType) {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("📝 AUDIT LOG - Stock Change Recorded");
        logger.info("───────────────────────────────────────────────────────────");
        logger.info("Timestamp:       {}", timestamp);
        logger.info("Action:          STOCK_{}", changeType);
        logger.info("Item ID:         {}", item.getId());
        logger.info("Item Name:       {}", item.getName());
        logger.info("Previous Stock:  {} units", oldQuantity);
        logger.info("New Stock:       {} units", newQuantity);
        logger.info("Change Amount:   {} {} units", change > 0 ? "+" : "", change);
        logger.info("Item Price:      ${}", item.getPrice());
        BigDecimal valueImpact = item.getPrice().multiply(new BigDecimal(Math.abs(change)));
        logger.info("Value Impact:    ${}", String.format("%.2f", valueImpact));

        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            logger.info("Category:        {}", item.getCategory());
        }

        // Log severity level
        if (newQuantity == 0) {
            logger.info("Severity:        CRITICAL - Out of Stock");
        } else if (newQuantity < 5) {
            logger.info("Severity:        HIGH - Low Stock");
        } else if (newQuantity < 10) {
            logger.info("Severity:        MEDIUM - Monitor Closely");
        } else {
            logger.info("Severity:        NORMAL");
        }

        logger.info("═══════════════════════════════════════════════════════════");

        // In production: Write to audit database table
        writeToAuditDatabase(timestamp, item, oldQuantity, newQuantity, change, changeType);
    }

    /**
     * Write audit log to database (simulated)
     */
    private void writeToAuditDatabase(String timestamp, Item item, int oldQuantity,
                                      int newQuantity, int change, String changeType) {
        // In production, save to audit_logs table
        logger.debug("💾 Audit record saved to database: STOCK_CHANGE_{} for item {}",
                changeType, item.getId());

        // Example SQL (for reference):
        // INSERT INTO audit_logs (timestamp, action_type, entity_type, entity_id, 
        //                         old_value, new_value, change_amount, user_id)
        // VALUES (?, 'STOCK_CHANGE', 'ITEM', ?, ?, ?, ?, ?);
    }

    @Override
    public String getObserverName() {
        return "AuditLogObserver";
    }
}
