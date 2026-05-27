package com.stockmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items")
public class BillItem {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    @JsonIgnoreProperties("items")
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Item item;

    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    // Parameterized constructor
    public BillItem(Bill bill, Item item, Integer quantity, BigDecimal unitPrice) {
        this();
        this.bill = bill;
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BillItem() {

    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // Legacy methods for backward compatibility
    public Item getProduct() {
        return item;
    }

    public void setProduct(Item item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // Helper method to calculate total price
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @Override
    public String toString() {
        return "BillItem{" +
                "id=" + id +
                ", bill=" + (bill != null ? bill.getId() : "null") +
                ", item=" + (item != null ? item.getId() : "null") +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }

}