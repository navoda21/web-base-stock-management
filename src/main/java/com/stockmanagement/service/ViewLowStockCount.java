package com.stockmanagement.service;

import com.stockmanagement.entity.Item;

import java.util.List;

public class ViewLowStockCount {

    private final ViewItemService viewItemService;

    public ViewLowStockCount(ViewItemService viewItemService) {
        this.viewItemService = viewItemService;
    }

    public void viewLowStockCountConsole() {
        List<Item> items = viewItemService.getAllItems();
        long lowStockCount = items.stream().filter(item -> item.getQuantity() < 5).count();
        System.out.println("Number of items with low stock (below 5): " + lowStockCount);
    }
}