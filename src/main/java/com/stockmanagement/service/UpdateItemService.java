package com.stockmanagement.service;

import com.stockmanagement.entity.Item;
import com.stockmanagement.repository.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateItemService {

    private final ItemRepository repository;

    public UpdateItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item updateItem(Item item) {
        return repository.save(item);
    }
}