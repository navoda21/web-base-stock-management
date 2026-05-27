package com.stockmanagement.observer.impl;

import com.stockmanagement.entity.Item;
import com.stockmanagement.observer.StockObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Observer that sends email notifications for significant stock changes
 * Observer Pattern - Concrete Observer
 */
@Component
public class EmailNotificationObserver implements StockObserver {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationObserver.class);
    private static final int SIGNIFICANT_CHANGE_THRESHOLD = 10;

    @Override
    public void onStockChange(Item item, int oldQuantity, int newQuantity) {
        int changeAmount = Math.abs(newQuantity - oldQuantity);

        // Send email for significant stock changes
        if (changeAmount >= SIGNIFICANT_CHANGE_THRESHOLD) {
            sendStockChangeEmail(item, oldQuantity, newQuantity, changeAmount);
        }

        // Send special email for stock depletion
        if (newQuantity == 0) {
            sendDepletionEmail(item);
        }

        // Send email for large stock additions (shipment received)
        if (newQuantity > oldQuantity && changeAmount >= 50) {
            sendShipmentReceivedEmail(item, changeAmount);
        }
    }

    /**
     * Send email notification for stock changes
     */
    private void sendStockChangeEmail(Item item, int oldQuantity, int newQuantity, int changeAmount) {
        String direction = newQuantity > oldQuantity ? "increased" : "decreased";

        logger.info("📧 EMAIL NOTIFICATION - Stock Update");
        logger.info("   To: inventory@company.com");
        logger.info("   Subject: Stock Update - {}", item.getName());
        logger.info("   Body:");
        logger.info("   ┌─────────────────────────────────────────────");
        logger.info("   │ Stock level {} for: {}", direction, item.getName());
        logger.info("   │ Item ID: {}", item.getId());
        logger.info("   │ Previous Quantity: {} units", oldQuantity);
        logger.info("   │ New Quantity: {} units", newQuantity);
        logger.info("   │ Change: {} {} units", newQuantity > oldQuantity ? "+" : "-", changeAmount);
        logger.info("   │ Price: ${}", item.getPrice());
        BigDecimal valueChange = item.getPrice().multiply(new BigDecimal(changeAmount));
        logger.info("   │ Value Change: ${}", String.format("%.2f", valueChange));
        logger.info("   └─────────────────────────────────────────────");

        // In production: Use JavaMailSender or email service
        // emailService.sendEmail("inventory@company.com", subject, body);
    }

    /**
     * Send urgent email for stock depletion
     */
    private void sendDepletionEmail(Item item) {
        logger.warn("📧 URGENT EMAIL - Stock Depleted");
        logger.warn("   To: purchasing@company.com, manager@company.com");
        logger.warn("   Subject: URGENT - {} is OUT OF STOCK", item.getName());
        logger.warn("   Priority: HIGH");
        logger.warn("   Body: Item {} (ID: {}) has run out of stock. Immediate restocking required.",
                item.getName(), item.getId());
    }

    /**
     * Send email notification for shipment received
     */
    private void sendShipmentReceivedEmail(Item item, int quantityAdded) {
        logger.info("📧 EMAIL - Shipment Received");
        logger.info("   To: warehouse@company.com");
        logger.info("   Subject: Shipment Received - {}", item.getName());
        logger.info("   Body: Received shipment of {} units for {}", quantityAdded, item.getName());
    }

    @Override
    public String getObserverName() {
        return "EmailNotificationObserver";
    }
}
