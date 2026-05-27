package com.stockmanagement.dto;

import com.stockmanagement.entity.Item;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PromotionWithItemsDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Set<Item> items = new HashSet<>();

    // Default constructor
    public PromotionWithItemsDTO() {
    }

    // Constructor for conversion from entity
    public PromotionWithItemsDTO(Long id, String name, String description, LocalDate startDate,
                                 LocalDate endDate, Boolean active, Set<Item> items) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.items = items != null ? items : new HashSet<>();
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIsActive() {
        return active;
    }

    public void setIsActive(Boolean isActive) {
        this.active = isActive;
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public boolean isCurrentlyActive() {
        if (active == null || !active) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}
