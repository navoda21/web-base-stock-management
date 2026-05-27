package com.stockmanagement.dto;

import com.stockmanagement.entity.Item;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PromotionDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Set<Long> itemIds = new HashSet<>();

    // Default constructor
    public PromotionDTO() {
    }

    // Constructor for conversion from entity
    public PromotionDTO(Long id, String name, String description, LocalDate startDate,
                        LocalDate endDate, Boolean active, Set<Item> items) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;

        if (items != null) {
            this.itemIds = items.stream()
                    .map(Item::getId)
                    .collect(Collectors.toSet());
        }
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

    // Add getIsActive() for backward compatibility
    public Boolean getIsActive() {
        return active;
    }

    public void setIsActive(Boolean isActive) {
        this.active = isActive;
    }

    // Helper method for template to get item count
    public int getItemCount() {
        return itemIds != null ? itemIds.size() : 0;
    }

    public Set<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(Set<Long> itemIds) {
        this.itemIds = itemIds;
    }
}