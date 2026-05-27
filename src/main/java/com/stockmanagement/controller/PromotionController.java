package com.stockmanagement.controller;

import com.stockmanagement.dto.PromotionDTO;
import com.stockmanagement.service.PromotionService;
import com.stockmanagement.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final ItemService itemService;

    @Autowired
    public PromotionController(PromotionService promotionService, ItemService itemService) {
        this.promotionService = promotionService;
        this.itemService = itemService;
    }

    @GetMapping
    public String listPromotions(Model model, @RequestParam(required = false) String keyword) {
        List<PromotionDTO> promotions;

        if (keyword != null && !keyword.trim().isEmpty()) {
            promotions = promotionService.searchPromotions(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            promotions = promotionService.getAllPromotions();
        }

        model.addAttribute("activeTab", "promotions");
        model.addAttribute("promotions", promotions);
        return "promotions/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("activeTab", "promotions");
        model.addAttribute("promotion", new PromotionDTO());
        model.addAttribute("allItems", itemService.getAllItems());
        return "promotions/create";
    }

    @PostMapping("/create")
    public String createPromotion(@ModelAttribute PromotionDTO promotionDTO,
                                  @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (itemIds != null && !itemIds.isEmpty()) {
                promotionDTO.setItemIds(new java.util.HashSet<>(itemIds));
            }
            PromotionDTO created = promotionService.createPromotion(promotionDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Promotion '" + created.getName() + "' created successfully with " +
                            created.getItemCount() + " item(s)");
            return "redirect:/promotions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("allItems", itemService.getAllItems());
            return "promotions/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return promotionService.getPromotionById(id)
                .map(promotion -> {
                    model.addAttribute("activeTab", "promotions");
                    model.addAttribute("promotion", promotion);
                    model.addAttribute("allItems", itemService.getAllItems());
                    return "promotions/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Promotion not found");
                    return "redirect:/promotions";
                });
    }

    @PostMapping("/edit/{id}")
    public String updatePromotion(@PathVariable Long id, @ModelAttribute PromotionDTO promotionDTO,
                                  @RequestParam(value = "itemIds", required = false) List<Long> itemIds,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (itemIds != null && !itemIds.isEmpty()) {
                promotionDTO.setItemIds(new java.util.HashSet<>(itemIds));
            } else {
                promotionDTO.setItemIds(new java.util.HashSet<>());
            }
            return promotionService.updatePromotion(id, promotionDTO)
                    .map(updated -> {
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Promotion '" + updated.getName() + "' updated successfully with " +
                                        updated.getItemCount() + " item(s)");
                        return "redirect:/promotions";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Promotion not found");
                        return "redirect:/promotions";
                    });
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("allItems", itemService.getAllItems());
            return "promotions/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (promotionService.deletePromotion(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Promotion deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Promotion not found");
        }
        return "redirect:/promotions";
    }

    @GetMapping("/active")
    public String listActivePromotions(Model model) {
        model.addAttribute("activeTab", "promotions");
        model.addAttribute("promotions", promotionService.getActivePromotions());
        model.addAttribute("title", "Active Promotions");
        return "promotions/list";
    }

    @GetMapping("/current")
    public String listCurrentPromotions(Model model) {
        model.addAttribute("activeTab", "promotions");
        model.addAttribute("promotions", promotionService.getCurrentlyActivePromotions());
        model.addAttribute("title", "Currently Running Promotions");
        return "promotions/list";
    }

    @GetMapping("/view/{id}")
    public String viewPromotion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return promotionService.getPromotionByIdWithItems(id)
                .map(promotion -> {
                    model.addAttribute("activeTab", "promotions");
                    model.addAttribute("promotion", promotion);
                    return "promotions/view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Promotion not found");
                    return "redirect:/promotions";
                });
    }
}