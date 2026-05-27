package com.stockmanagement.service;

import com.stockmanagement.dto.PromotionDTO;
import com.stockmanagement.dto.PromotionWithItemsDTO;
import com.stockmanagement.entity.Item;
import com.stockmanagement.entity.Promotion;
import com.stockmanagement.repository.ItemRepository;
import com.stockmanagement.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository, ItemRepository itemRepository) {
        this.promotionRepository = promotionRepository;
        this.itemRepository = itemRepository;
    }

    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getCurrentlyActivePromotions() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findCurrentlyActivePromotions(today).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PromotionDTO> getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<PromotionWithItemsDTO> getPromotionByIdWithItems(Long id) {
        return promotionRepository.findById(id)
                .map(this::convertToDTOWithItems);
    }

    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        Promotion promotion = new Promotion();
        updatePromotionFromDTO(promotion, promotionDTO);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDTO(savedPromotion);
    }

    @Transactional
    public Optional<PromotionDTO> updatePromotion(Long id, PromotionDTO promotionDTO) {
        return promotionRepository.findById(id)
                .map(existingPromotion -> {
                    updatePromotionFromDTO(existingPromotion, promotionDTO);
                    return convertToDTO(promotionRepository.save(existingPromotion));
                });
    }

    @Transactional
    public boolean deletePromotion(Long id) {
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<PromotionDTO> searchPromotions(String keyword) {
        return promotionRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getPromotionsForItem(Long itemId) {
        return promotionRepository.findByItemId(itemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        return new PromotionDTO(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getActive(),
                promotion.getItems()
        );
    }

    private PromotionWithItemsDTO convertToDTOWithItems(Promotion promotion) {
        return new PromotionWithItemsDTO(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.getActive(),
                promotion.getItems()
        );
    }

    private void updatePromotionFromDTO(Promotion promotion, PromotionDTO dto) {
        // Validate dates: End date cannot be before start date
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            if (dto.getEndDate().isBefore(dto.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date. Start: " +
                        dto.getStartDate() + ", End: " + dto.getEndDate());
            }
        }

        promotion.setName(dto.getName());
        promotion.setDescription(dto.getDescription());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());
        promotion.setActive(dto.getActive());

        // Handle item associations
        Set<Item> items = new HashSet<>();
        if (dto.getItemIds() != null && !dto.getItemIds().isEmpty()) {
            for (Long itemId : dto.getItemIds()) {
                itemRepository.findById(itemId).ifPresent(items::add);
            }
        }
        promotion.setItems(items);
    }
}