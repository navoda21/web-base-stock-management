package com.stockmanagement.service;

import com.stockmanagement.entity.Item;
import com.stockmanagement.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ViewItemService {

    private final ItemRepository repository;

    public ViewItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public List<Item> getAllItems() {
        return repository.findAll();
    }

    public Optional<Item> getItemById(int id) {
        return repository.findById((long) id);
    }
}