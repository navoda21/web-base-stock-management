package com.stockmanagement.controller;


import com.stockmanagement.entity.Item;
import com.stockmanagement.entity.SupplierItem;
import com.stockmanagement.service.AddItemService;
import com.stockmanagement.service.DeleteItemService;
import com.stockmanagement.service.ItemService;
import com.stockmanagement.service.SupplierItemService;
import com.stockmanagement.service.SupplierRequestService;
import com.stockmanagement.service.UpdateItemService;
import com.stockmanagement.service.ViewItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private AddItemService addItemService;

    @Autowired
    private ViewItemService viewItemService;

    @Autowired
    private UpdateItemService updateItemService;

    @Autowired
    private DeleteItemService deleteItemService;

    @Autowired
    private SupplierItemService supplierItemService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SupplierRequestService supplierRequestService;

    @GetMapping
    public String home() {
        return "redirect:/"; // serve static index.html at root
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "items/dashboard";
    }

    @GetMapping("/list")
    public String listItems(Model model) {
        List<Item> items = viewItemService.getAllItems();
        model.addAttribute("items", items);
        return "items/dashboard";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public List<Item> listItemsApi() {
        return viewItemService.getAllItems();
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addItem(@RequestBody Item item, Model model) {
        try {
            addItemService.addItem(item);
            return ResponseEntity.ok("Item added successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding item: " + e.getMessage());
        }
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<String> addItemApi(@RequestBody Item item) {
        try {
            addItemService.addItem(item);
            return ResponseEntity.ok("Item added successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding item: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<String> updateItem(@PathVariable int id, @RequestBody Item item, Model model) {
        try {
            item.setId(id);
            updateItemService.updateItem(item);
            return ResponseEntity.ok("Item updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating item: " + e.getMessage());
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<String> updateItemApi(@PathVariable int id, @RequestBody Item item) {
        try {
            item.setId(id);
            updateItemService.updateItem(item);
            return ResponseEntity.ok("Item updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating item: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteItem(@PathVariable int id, Model model) {
        try {
            deleteItemService.deleteItem(id);
            return ResponseEntity.ok("Item deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting item: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteItemApi(@PathVariable int id) {
        try {
            deleteItemService.deleteItem(id);
            return ResponseEntity.ok("Item deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting item: " + e.getMessage());
        }
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("item", new Item());
        return "items/add";
    }

    @PostMapping("/add/form")
    public String addItemForm(@ModelAttribute Item item, Model model) {
        try {
            addItemService.addItem(item);
            model.addAttribute("message", "Item added successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error adding item: " + e.getMessage());
        }
        return "redirect:/items/list";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        try {
            Optional<Item> optionalItem = viewItemService.getItemById(id.intValue());
            Item item = optionalItem.orElseThrow(() -> new RuntimeException("Item not found with ID: " + id));
            model.addAttribute("item", item);
            return "items/edit";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/items/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching item: " + e.getMessage());
            return "redirect:/items/dashboard";
        }
    }

    @PostMapping("/update/{id}")
    public String updateItemForm(@PathVariable Long id, @ModelAttribute Item item, Model model) {
        try {
            item.setId(id);
            updateItemService.updateItem(item);
            model.addAttribute("message", "Item updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error updating item: " + e.getMessage());
        }
        return "redirect:/items/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteItemForm(@PathVariable Long id, Model model) {
        try {
            deleteItemService.deleteItem(id.intValue());
            model.addAttribute("message", "Item deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting item: " + e.getMessage());
        }
        return "redirect:/items/dashboard";
    }

    @GetMapping("/view/{id}")
    public String viewItemDetails(@PathVariable Long id, Model model) {
        try {
            Optional<Item> optionalItem = viewItemService.getItemById(id.intValue());
            Item item = optionalItem.orElseThrow(() -> new RuntimeException("Item not found with ID: " + id));

            // Get suppliers for this item
            List<SupplierItem> suppliers = supplierItemService.getSuppliersByItem(id);

            model.addAttribute("item", item);
            model.addAttribute("supplierItems", suppliers);
            return "items/view-details";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching item: " + e.getMessage());
            return "redirect:/items/dashboard";
        }
    }

    /**
     * Get low stock items (API endpoint for notifications)
     */
    @GetMapping("/api/low-stock")
    @ResponseBody
    public ResponseEntity<List<Item>> getLowStockItems(@RequestParam(defaultValue = "10") int threshold) {
        try {
            List<Item> lowStockItems = itemService.getLowStockItems(threshold);
            return ResponseEntity.ok(lowStockItems);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Create a supplier request for low stock item
     */
    @PostMapping("/api/request-from-supplier/{itemId}")
    @ResponseBody
    public ResponseEntity<String> requestFromSupplier(
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @RequestParam(required = false) String notes) {
        try {
            supplierRequestService.createRequest(itemId, quantity, "System", notes);
            return ResponseEntity.ok("Supplier request created successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating supplier request: " + e.getMessage());
        }
    }
}
