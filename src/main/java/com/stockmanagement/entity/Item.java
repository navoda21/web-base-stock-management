package com.stockmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String category;

    @Column(unique = true, length = 50)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @JsonIgnore
    @ManyToMany(mappedBy = "items")
    private Set<Promotion> promotions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Discount> discounts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierItem> supplierItems = new ArrayList<>();

    public Item() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.quantity = 0;
    }

    public Item(Long id, String name, Integer quantity, BigDecimal price, String category) {
        this();
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Backward compatibility for int id
    public void setId(int id) {
        this.id = (long) id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Backward compatibility for double price
    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Set<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(Set<Promotion> promotions) {
        this.promotions = promotions;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<Discount> discounts) {
        this.discounts = discounts;
    }

    // Helper methods for discounts and promotions

    public void addDiscount(Discount discount) {
        discounts.add(discount);
        discount.setItem(this);
    }

    public void removeDiscount(Discount discount) {
        discounts.remove(discount);
        discount.setItem(null);
    }

    public List<SupplierItem> getSupplierItems() {
        return supplierItems;
    }

    public void setSupplierItems(List<SupplierItem> supplierItems) {
        this.supplierItems = supplierItems;
    }

    // Helper methods for supplier items
    public void addSupplierItem(SupplierItem supplierItem) {
        supplierItems.add(supplierItem);
        supplierItem.setItem(this);
    }

    public void removeSupplierItem(SupplierItem supplierItem) {
        supplierItems.remove(supplierItem);
        supplierItem.setItem(null);
    }

    public BigDecimal getCurrentPrice() {
        // Apply active discounts (using the highest discount only)
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (Discount discount : discounts) {
            if (discount.isCurrentlyActive()) {
                BigDecimal discountedPrice = discount.getDiscountedPrice(price);
                BigDecimal discountAmount = price.subtract(discountedPrice);
                if (discountAmount.compareTo(maxDiscount) > 0) {
                    maxDiscount = discountAmount;
                }
            }
        }

        BigDecimal finalPrice = price.subtract(maxDiscount);
        return finalPrice.max(BigDecimal.ZERO);
    }

    public int getPromotionCount() {
        return promotions != null ? (int) promotions.stream().filter(p -> p.isActive()).count() : 0;
    }

    public int getDiscountCount() {
        return discounts != null ? (int) discounts.stream().filter(d -> d.isCurrentlyActive()).count() : 0;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}