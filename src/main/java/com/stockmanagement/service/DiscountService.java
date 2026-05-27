package com.stockmanagement.service;

import com.stockmanagement.dto.DiscountDTO;
import com.stockmanagement.entity.Discount;
import com.stockmanagement.repository.DiscountRepository;
import com.stockmanagement.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public DiscountService(DiscountRepository discountRepository, ItemRepository itemRepository) {
        this.discountRepository = discountRepository;
        this.itemRepository = itemRepository;
    }

    public List<DiscountDTO> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DiscountDTO> getActiveDiscounts() {
        return discountRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DiscountDTO> getCurrentlyActiveDiscounts() {
        LocalDate today = LocalDate.now();
        return discountRepository.findCurrentlyActiveDiscounts(today).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<DiscountDTO> getDiscountById(Long id) {
        return discountRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public DiscountDTO createDiscount(DiscountDTO discountDTO) {
        Discount discount = new Discount();
        updateDiscountFromDTO(discount, discountDTO);
        Discount savedDiscount = discountRepository.save(discount);
        return convertToDTO(savedDiscount);
    }

    @Transactional
    public Optional<DiscountDTO> updateDiscount(Long id, DiscountDTO discountDTO) {
        return discountRepository.findById(id)
                .map(existingDiscount -> {
                    updateDiscountFromDTO(existingDiscount, discountDTO);
                    return convertToDTO(discountRepository.save(existingDiscount));
                });
    }

    @Transactional
    public boolean deleteDiscount(Long id) {
        if (discountRepository.existsById(id)) {
            discountRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<DiscountDTO> searchDiscounts(String keyword) {
        return discountRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DiscountDTO> getDiscountsForItem(Long itemId) {
        return discountRepository.findByItemId(itemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DiscountDTO convertToDTO(Discount discount) {
        DiscountDTO dto = new DiscountDTO();
        dto.setId(discount.getId());
        dto.setName(discount.getName());
        dto.setDescription(discount.getDescription());
        dto.setType(discount.getType());
        dto.setValue(discount.getValue());
        dto.setStartDate(discount.getStartDate());
        dto.setEndDate(discount.getEndDate());
        dto.setIsActive(discount.getIsActive());

        if (discount.getItem() != null) {
            dto.setItemId(discount.getItem().getId());
            dto.setItemName(discount.getItem().getName());
        }

        return dto;
    }

    private void updateDiscountFromDTO(Discount discount, DiscountDTO dto) {
        discount.setName(dto.getName());
        discount.setDescription(dto.getDescription());
        discount.setType(dto.getType());
        discount.setValue(dto.getValue());
        discount.setStartDate(dto.getStartDate());
        discount.setEndDate(dto.getEndDate());
        discount.setIsActive(dto.getIsActive());

        // Handle item association
        if (dto.getItemId() != null) {
            itemRepository.findById(dto.getItemId())
                    .ifPresent(discount::setItem);
        }
    }
}