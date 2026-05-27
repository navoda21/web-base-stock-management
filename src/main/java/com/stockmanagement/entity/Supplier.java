package com.stockmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String contactNo;
    private String email;
    private int rating;

    @JsonIgnore
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierItem> supplierItems = new ArrayList<>();

    public Supplier() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<SupplierItem> getSupplierItems() {
        return supplierItems;
    }

    public void setSupplierItems(List<SupplierItem> supplierItems) {
        this.supplierItems = supplierItems;
    }

    // Helper methods to manage the relationship
    public void addSupplierItem(SupplierItem supplierItem) {
        supplierItems.add(supplierItem);
        supplierItem.setSupplier(this);
    }

    public void removeSupplierItem(SupplierItem supplierItem) {
        supplierItems.remove(supplierItem);
        supplierItem.setSupplier(null);
    }
}
