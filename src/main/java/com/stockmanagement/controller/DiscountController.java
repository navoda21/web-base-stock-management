package com.stockmanagement.controller;

import com.stockmanagement.dto.DiscountDTO;
import com.stockmanagement.entity.DiscountType;
import com.stockmanagement.service.DiscountService;
import com.stockmanagement.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/discounts")
public class DiscountController {

    private final DiscountService discountService;
    private final ItemService itemService;

    @Autowired
    public DiscountController(DiscountService discountService, ItemService itemService) {
        this.discountService = discountService;
        this.itemService = itemService;
    }

    @GetMapping
    public String listDiscounts(Model model, @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("discounts", discountService.searchDiscounts(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("discounts", discountService.getAllDiscounts());
        }
        model.addAttribute("activeTab", "discounts");
        return "discounts/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("activeTab", "discounts");
        model.addAttribute("discount", new DiscountDTO());
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("items", itemService.getAllItems());
        return "discounts/create";
    }

    @PostMapping("/create")
    public String createDiscount(@ModelAttribute DiscountDTO discountDTO,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        try {
            // Validate dates
            if (discountDTO.getStartDate() != null && discountDTO.getEndDate() != null) {
                if (discountDTO.getEndDate().isBefore(discountDTO.getStartDate())) {
                    model.addAttribute("errorMessage", "End date cannot be before start date");
                    model.addAttribute("discount", discountDTO);
                    model.addAttribute("discountTypes", DiscountType.values());
                    model.addAttribute("items", itemService.getAllItems());
                    model.addAttribute("activeTab", "discounts");
                    return "discounts/create";
                }
            }

            DiscountDTO created = discountService.createDiscount(discountDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Discount '" + created.getName() + "' created successfully");
            return "redirect:/discounts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating discount: " + e.getMessage());
            model.addAttribute("discount", discountDTO);
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("items", itemService.getAllItems());
            model.addAttribute("activeTab", "discounts");
            return "discounts/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return discountService.getDiscountById(id)
                .map(discount -> {
                    model.addAttribute("activeTab", "discounts");
                    model.addAttribute("discount", discount);
                    model.addAttribute("discountTypes", DiscountType.values());
                    model.addAttribute("items", itemService.getAllItems());
                    return "discounts/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Discount not found");
                    return "redirect:/discounts";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateDiscount(@PathVariable Long id, @ModelAttribute DiscountDTO discountDTO,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        try {
            // Validate dates
            if (discountDTO.getStartDate() != null && discountDTO.getEndDate() != null) {
                if (discountDTO.getEndDate().isBefore(discountDTO.getStartDate())) {
                    model.addAttribute("errorMessage", "End date cannot be before start date");
                    model.addAttribute("discount", discountDTO);
                    model.addAttribute("discountTypes", DiscountType.values());
                    model.addAttribute("items", itemService.getAllItems());
                    model.addAttribute("activeTab", "discounts");
                    return "discounts/edit";
                }
            }

            return discountService.updateDiscount(id, discountDTO)
                    .map(updated -> {
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Discount '" + updated.getName() + "' updated successfully");
                        return "redirect:/discounts";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Discount not found");
                        return "redirect:/discounts";
                    });
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating discount: " + e.getMessage());
            model.addAttribute("discount", discountDTO);
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("items", itemService.getAllItems());
            model.addAttribute("activeTab", "discounts");
            return "discounts/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (discountService.deleteDiscount(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "Discount deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Discount not found");
        }
        return "redirect:/discounts";
    }

    @GetMapping("/active")
    public String listActiveDiscounts(Model model) {
        model.addAttribute("activeTab", "discounts");
        model.addAttribute("discounts", discountService.getActiveDiscounts());
        model.addAttribute("title", "Active Discounts");
        return "discounts/list";
    }

    @GetMapping("/current")
    public String listCurrentDiscounts(Model model) {
        model.addAttribute("activeTab", "discounts");
        model.addAttribute("discounts", discountService.getCurrentlyActiveDiscounts());
        model.addAttribute("title", "Currently Running Discounts");
        return "discounts/list";
    }

    @GetMapping("/item/{itemId}")
    public String listDiscountsForItem(@PathVariable Long itemId, Model model) {
        model.addAttribute("activeTab", "discounts");
        model.addAttribute("discounts", discountService.getDiscountsForItem(itemId));
        model.addAttribute("item", itemService.getItemById(itemId).orElse(null));
        return "discounts/item-discounts";
    }
}