package com.stockmanagement.service;

import com.stockmanagement.entity.Item;

import java.util.List;

public class LowStockAlert {

    private final ViewItemService viewItemService;

    public LowStockAlert(ViewItemService viewItemService) {
        this.viewItemService = viewItemService;
    }

    public void lowStockAlertConsole() {
        List<Item> items = viewItemService.getAllItems();
        boolean hasLowStock = false;
        StringBuilder alert = new StringBuilder("Low Stock Alerts: ");
        for (Item item : items) {
            if (item.getQuantity() < 5) {
                alert.append(item.getName()).append(" (Qty: ").append(item.getQuantity()).append("), ");
                hasLowStock = true;
            }
        }
        if (hasLowStock) {
            alert.delete(alert.length() - 2, alert.length());
            System.out.println(alert.toString());
        } else {
            System.out.println("No low stock items.");
        }
    }
}