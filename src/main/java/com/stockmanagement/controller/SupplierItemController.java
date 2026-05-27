package com.stockmanagement.controller;

import com.stockmanagement.entity.Item;
import com.stockmanagement.entity.Supplier;
import com.stockmanagement.entity.SupplierItem;
import com.stockmanagement.repository.ItemRepository;
import com.stockmanagement.repository.SupplierRepository;
import com.stockmanagement.service.SupplierItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/supplier-items")
public class SupplierItemController {

    @Autowired
    private SupplierItemService supplierItemService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * List all supplier-item relationships
     */
    @GetMapping
    public String listSupplierItems(Model model) {
        List<SupplierItem> supplierItems = supplierItemService.getAllSupplierItems();
        model.addAttribute("supplierItems", supplierItems);
        return "supplier-items/list";
    }

    /**
     * Show form to add a new supplier-item relationship
     */
    @GetMapping("/add")
    public String showAddForm(@RequestParam(required = false) Long supplierId,
                              @RequestParam(required = false) Long itemId,
                              Model model) {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<Item> items = itemRepository.findAll();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("items", items);
        model.addAttribute("supplierItem", new SupplierItem());
        model.addAttribute("preSelectedSupplierId", supplierId);
        model.addAttribute("preSelectedItemId", itemId);

        return "supplier-items/add";
    }

    /**
     * Add a new supplier-item relationship
     */
    @PostMapping("/add")
    public String addSupplierItem(@RequestParam Long supplierId,
                                  @RequestParam Long itemId,
                                  @RequestParam Integer suppliedQuantity,
                                  @RequestParam(required = false) BigDecimal supplyPrice,
                                  @RequestParam(required = false) Boolean isPrimarySupplier,
                                  @RequestParam(required = false) Integer leadTimeDays,
                                  @RequestParam(required = false) Integer minOrderQuantity,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes redirectAttributes) {
        try {
            SupplierItem supplierItem = supplierItemService.addSupplierItem(supplierId, itemId, suppliedQuantity, supplyPrice);

            // Update additional fields if provided
            if (isPrimarySupplier != null || leadTimeDays != null || minOrderQuantity != null || notes != null) {
                supplierItemService.updateSupplierItem(
                        supplierItem.getId(),
                        null,
                        null,
                        isPrimarySupplier,
                        leadTimeDays,
                        minOrderQuantity,
                        notes
                );
            }

            redirectAttributes.addFlashAttribute("successMessage", "Supplier-Item relationship added successfully!");
            return "redirect:/supplier-items";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/supplier-items/add";
        }
    }

    /**
     * Show form to edit a supplier-item relationship
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return supplierItemService.getSupplierItemById(id)
                .map(supplierItem -> {
                    model.addAttribute("supplierItem", supplierItem);
                    return "supplier-items/edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "SupplierItem not found!");
                    return "redirect:/supplier-items";
                });
    }

    /**
     * Update a supplier-item relationship
     */
    @PostMapping("/edit/{id}")
    public String updateSupplierItem(@PathVariable Long id,
                                     @RequestParam Integer suppliedQuantity,
                                     @RequestParam(required = false) BigDecimal supplyPrice,
                                     @RequestParam(required = false) Boolean isPrimarySupplier,
                                     @RequestParam(required = false) Integer leadTimeDays,
                                     @RequestParam(required = false) Integer minOrderQuantity,
                                     @RequestParam(required = false) String notes,
                                     RedirectAttributes redirectAttributes) {
        try {
            supplierItemService.updateSupplierItem(id, suppliedQuantity, supplyPrice,
                    isPrimarySupplier, leadTimeDays,
                    minOrderQuantity, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier-Item relationship updated successfully!");
            return "redirect:/supplier-items";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/supplier-items/edit/" + id;
        }
    }

    /**
     * Delete a supplier-item relationship
     */
    @GetMapping("/delete/{id}")
    public String deleteSupplierItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierItemService.removeSupplierItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier-Item relationship deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting relationship: " + e.getMessage());
        }
        return "redirect:/supplier-items";
    }

    /**
     * View suppliers for a specific item
     */
    @GetMapping("/item/{itemId}")
    public String viewSuppliersForItem(@PathVariable Long itemId, Model model, RedirectAttributes redirectAttributes) {
        return itemRepository.findById(itemId)
                .map(item -> {
                    List<SupplierItem> suppliers = supplierItemService.getSuppliersByItem(itemId);
                    model.addAttribute("item", item);
                    model.addAttribute("supplierItems", suppliers);
                    return "supplier-items/item-suppliers";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Item not found!");
                    return "redirect:/items";
                });
    }

    /**
     * View items for a specific supplier
     */
    @GetMapping("/supplier/{supplierId}")
    public String viewItemsForSupplier(@PathVariable Long supplierId, Model model, RedirectAttributes redirectAttributes) {
        return supplierRepository.findById(supplierId)
                .map(supplier -> {
                    List<SupplierItem> items = supplierItemService.getItemsBySupplier(supplierId);
                    model.addAttribute("supplier", supplier);
                    model.addAttribute("supplierItems", items);
                    return "supplier-items/supplier-items";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Supplier not found!");
                    return "redirect:/suppliers";
                });
    }
}
