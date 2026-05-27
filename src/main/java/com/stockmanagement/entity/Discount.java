package com.stockmanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "discounts")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;

    @Column(nullable = false)
    private Double value;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "active")
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    // Standard constructors, getters, and setters

    public Discount() {
    }

    public Discount(String name, String description, DiscountType type, Double value,
                    LocalDate startDate, LocalDate endDate, Item item) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
        this.item = item;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return active;
    }

    public void setIsActive(Boolean isActive) {
        this.active = isActive;
    }

    // Add direct getter and setter for the 'active' field
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // Helper methods

    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return active && !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public double getDiscountedPrice(double originalPrice) {
        if (!isCurrentlyActive()) {
            return originalPrice;
        }

        if (type == DiscountType.PERCENTAGE) {
            return originalPrice - (originalPrice * value / 100.0);
        } else { // FIXED_AMOUNT
            return Math.max(0, originalPrice - value);
        }
    }

    public java.math.BigDecimal getDiscountedPrice(java.math.BigDecimal originalPrice) {
        if (!isCurrentlyActive()) {
            return originalPrice;
        }

        java.math.BigDecimal valueBD = java.math.BigDecimal.valueOf(value);

        if (type == DiscountType.PERCENTAGE) {
            java.math.BigDecimal discountAmount = originalPrice.multiply(valueBD).divide(java.math.BigDecimal.valueOf(100.0));
            return originalPrice.subtract(discountAmount);
        } else { // FIXED_AMOUNT
            java.math.BigDecimal result = originalPrice.subtract(valueBD);
            return result.max(java.math.BigDecimal.ZERO);
        }
    }
}