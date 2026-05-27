package com.stockmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Junction entity to represent the many-to-many relationship between Suppliers and Items.
 * This entity stores additional information about what items each supplier provides,
 * including quantity and supply price.
 */
@Entity
@Table(name = "supplier_items")
public class SupplierItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * The quantity of this item that the supplier can provide
     */
    @Column(name = "supplied_quantity", nullable = false)
    private Integer suppliedQuantity;

    /**
     * The price at which the supplier provides this item (wholesale/supply price)
     */
    @Column(name = "supply_price", precision = 10, scale = 2)
    private BigDecimal supplyPrice;

    /**
     * Indicates if this supplier is currently the primary/preferred supplier for this item
     */
    @Column(name = "is_primary_supplier")
    private Boolean isPrimarySupplier;

    /**
     * Lead time in days for this supplier to deliver this item
     */
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    /**
     * Minimum order quantity for this item from this supplier
     */
    @Column(name = "min_order_quantity")
    private Integer minOrderQuantity;

    /**
     * Date when this supply relationship was established
     */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Date when this supply relationship was last updated
     */
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    /**
     * Additional notes about the supplier-item relationship
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    public SupplierItem() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.isPrimarySupplier = false;
        this.suppliedQuantity = 0;
    }

    public SupplierItem(Supplier supplier, Item item, Integer suppliedQuantity, BigDecimal supplyPrice) {
        this();
        this.supplier = supplier;
        this.item = item;
        this.suppliedQuantity = suppliedQuantity;
        this.supplyPrice = supplyPrice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getSuppliedQuantity() {
        return suppliedQuantity;
    }

    public void setSuppliedQuantity(Integer suppliedQuantity) {
        this.suppliedQuantity = suppliedQuantity;
    }

    public BigDecimal getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(BigDecimal supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public Boolean getIsPrimarySupplier() {
        return isPrimarySupplier;
    }

    public void setIsPrimarySupplier(Boolean isPrimarySupplier) {
        this.isPrimarySupplier = isPrimarySupplier;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public Integer getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(Integer minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    @Override
    public String toString() {
        return "SupplierItem{" +
                "id=" + id +
                ", suppliedQuantity=" + suppliedQuantity +
                ", supplyPrice=" + supplyPrice +
                ", isPrimarySupplier=" + isPrimarySupplier +
                '}';
    }
}
