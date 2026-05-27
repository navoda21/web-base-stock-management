package com.stockmanagement.service;

import com.stockmanagement.entity.Item;
import com.stockmanagement.repository.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class AddItemService {

    private final ItemRepository repository;

    public AddItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public void addItem(Item item) {
        repository.save(item);
    }
}