package com.stockmanagement.controller;

import com.stockmanagement.entity.Supplier;
import com.stockmanagement.entity.SupplierItem;
import com.stockmanagement.entity.SupplierRequest;
import com.stockmanagement.service.SupplierItemService;
import com.stockmanagement.service.SupplierRequestService;
import com.stockmanagement.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService service;

    @Autowired
    private SupplierItemService supplierItemService;

    @Autowired
    private SupplierRequestService supplierRequestService;

    @GetMapping
    public String dashboard() {
        return "supplier/index";
    }

    @GetMapping("/all")
    @ResponseBody
    public List<Supplier> getAllSuppliers() {
        return service.getAllSuppliers();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Supplier> getSupplier(@PathVariable Long id) {
        try {
            Supplier supplier = service.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Supplier> addSupplier(@RequestBody Supplier supplier) {
        try {
            Supplier added = service.addSupplier(supplier);
            return ResponseEntity.ok(added);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier updated) {
        try {
            Supplier result = service.updateSupplier(id, updated);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            service.deleteSupplier(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/view/{id}")
    public String viewSupplierDetails(@PathVariable Long id, Model model) {
        try {
            Supplier supplier = service.getSupplierById(id);
            List<SupplierItem> suppliedItems = supplierItemService.getItemsBySupplier(id);

            model.addAttribute("supplier", supplier);
            model.addAttribute("supplierItems", suppliedItems);
            return "supplier/view-details";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching supplier: " + e.getMessage());
            return "redirect:/suppliers";
        }
    }

    /**
     * Get all pending supplier requests
     */
    @GetMapping("/api/requests/pending")
    @ResponseBody
    public ResponseEntity<List<SupplierRequest>> getPendingRequests() {
        try {
            List<SupplierRequest> requests = supplierRequestService.getPendingRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all supplier requests
     */
    @GetMapping("/api/requests/all")
    @ResponseBody
    public ResponseEntity<List<SupplierRequest>> getAllRequests() {
        try {
            List<SupplierRequest> requests = supplierRequestService.getAllRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update supplier request status
     */
    @PutMapping("/api/requests/{requestId}/status")
    @ResponseBody
    public ResponseEntity<String> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestParam String status,
            @RequestParam(required = false) String processedBy) {
        try {
            supplierRequestService.updateRequestStatus(requestId, status, processedBy != null ? processedBy : "System");
            return ResponseEntity.ok("Request status updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating request: " + e.getMessage());
        }
    }
}