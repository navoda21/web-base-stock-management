package com.stockmanagement.service;

import com.stockmanagement.entity.Supplier;
import com.stockmanagement.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ViewSupplierService {
    private final SupplierRepository repository;

    public ViewSupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    public List<Supplier> getAllSuppliers() {
        return repository.findAll();
    }

    public Supplier getSupplierById(Long id) {
        return repository.findById(id).orElse(null);
    }
}

