package com.stockmanagement.service;

import com.stockmanagement.entity.Supplier;
import com.stockmanagement.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository repo;

    public SupplierService(SupplierRepository repo) {
        this.repo = repo;
    }

    // Add Supplier
    public Supplier addSupplier(Supplier s) {
        return repo.save(s);
    }

    // Get All Suppliers
    public List<Supplier> getAllSuppliers() {
        return repo.findAll();
    }

    // Get Supplier by ID
    public Supplier getSupplierById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    // Update Supplier
    public Supplier updateSupplier(Long id, Supplier updated) {
        Supplier existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setContactNo(updated.getContactNo());
        existing.setEmail(updated.getEmail());
        existing.setRating(updated.getRating());

        return repo.save(existing);
    }

    // Delete Supplier
    public void deleteSupplier(Long id) {
        repo.deleteById(id);
    }
}
