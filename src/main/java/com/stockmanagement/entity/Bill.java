package com.stockmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "bill_number", unique = true, nullable = false)
    private String billNumber;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "subtotal_amount")
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "bill_date")
    private LocalDateTime billDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BillStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("bill")
    private List<BillItem> items = new ArrayList<>();

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Default constructor
    public Bill() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.billDate = LocalDateTime.now();
        this.status = BillStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.subtotalAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.amountPaid = BigDecimal.ZERO;
    }

    // Parameterized constructor
    public Bill(Customer customer, String billNumber, BigDecimal totalAmount) {
        this();
        this.customer = customer;
        this.billNumber = billNumber;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<BillItem> getItems() {
        return items;
    }

    public void setItems(List<BillItem> items) {
        this.items = items;
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

    // Helper method to add bill item
    public void addBillItem(BillItem item) {
        items.add(item);
        item.setBill(this);
    }

    // Helper method to remove bill item
    public void removeBillItem(BillItem item) {
        items.remove(item);
        item.setBill(null);
    }

    // Compatibility method for template (list.html)
    public String getPaymentStatus() {
        return status != null ? status.name() : "PENDING";
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getId() : "null") +
                ", billNumber='" + billNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", billDate=" + billDate +
                ", status=" + status +
                ", createdDate=" + createdDate +
                '}';
    }
}
