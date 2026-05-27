package com.stockmanagement.dto;

import java.math.BigDecimal;

public class BillItemRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public BillItemRequest() {
        this.quantity = 0;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity != null ? quantity : 0;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "BillItemRequest{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}