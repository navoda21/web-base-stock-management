package com.stockmanagement.dto;

import java.util.ArrayList;
import java.util.List;

public class BillRequest {
    private Long customerId;
    private List<BillItemRequest> items = new ArrayList<>();
    private String notes;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<BillItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BillItemRequest> items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Builder method to help construct the request from controller parameters
    public static BillRequest create(Long customerId, List<Long> productIds, List<Integer> quantities, List<Double> unitPrices, String notes) {
        BillRequest request = new BillRequest();
        request.setCustomerId(customerId);
        request.setNotes(notes);

        if (productIds != null && quantities != null && unitPrices != null) {
            int size = Math.min(Math.min(productIds.size(), quantities.size()), unitPrices.size());
            for (int i = 0; i < size; i++) {
                BillItemRequest item = new BillItemRequest();
                item.setProductId(productIds.get(i));
                item.setQuantity(quantities.get(i));
                item.setUnitPrice(java.math.BigDecimal.valueOf(unitPrices.get(i)));
                request.getItems().add(item);
            }
        }

        return request;
    }

    @Override
    public String toString() {
        return "BillRequest{" +
                "customerId=" + customerId +
                ", items=" + items +
                ", notes='" + notes + '\'' +
                '}';
    }
}