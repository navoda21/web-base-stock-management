package com.stockmanagement.controller;

import com.stockmanagement.entity.Customer;
import com.stockmanagement.service.CustomerService;
import com.stockmanagement.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerWebController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<Customer> allCustomers = customerService.getAllCustomers();

        // Manual pagination since we're not using Spring Data repositories directly
        int start = page * size;
        int end = Math.min((start + size), allCustomers.size());

        List<Customer> paginatedCustomers = allCustomers.subList(start, end);
        Page<Customer> customerPage = new PageImpl<>(paginatedCustomers, PageRequest.of(page, size), allCustomers.size());

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());

        return "customers/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/create";
    }

    @PostMapping("/create")
    public String createCustomer(
            @ModelAttribute Customer customer,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        try {
            // Handle photo upload if provided
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = fileStorageService.storeFile(photo, "customer-photos");
                customer.setPhotoUrl(photoUrl);
                customer.setPhotoThumbnailUrl(photoUrl); // For simplicity, using same URL
            }

            Customer savedCustomer = customerService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Customer " + savedCustomer.getFirstName() + " " + savedCustomer.getLastName() + " created successfully");

            return "redirect:/customers";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("customer", customer);
            return "redirect:/customers/create";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading photo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("customer", customer);
            return "redirect:/customers/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating customer: " + e.getMessage());
            redirectAttributes.addFlashAttribute("customer", customer);
            return "redirect:/customers/create";
        }
    }

    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);

        if (customerOpt.isPresent()) {
            model.addAttribute("customer", customerOpt.get());
            return "customers/view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
            return "redirect:/customers";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);

        if (customerOpt.isPresent()) {
            model.addAttribute("customer", customerOpt.get());
            return "customers/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
            return "redirect:/customers";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCustomer(
            @PathVariable Long id,
            @ModelAttribute Customer customer,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        try {
            // Get existing customer to preserve photo URL if no new photo
            Optional<Customer> existingCustomerOpt = customerService.getCustomerById(id);

            if (existingCustomerOpt.isPresent()) {
                Customer existingCustomer = existingCustomerOpt.get();

                // If no new photo is uploaded, keep the existing photo
                if ((photo == null || photo.isEmpty()) && existingCustomer.getPhotoUrl() != null) {
                    customer.setPhotoUrl(existingCustomer.getPhotoUrl());
                    customer.setPhotoThumbnailUrl(existingCustomer.getPhotoThumbnailUrl());
                } else if (photo != null && !photo.isEmpty()) {
                    // If a new photo is uploaded, delete old photo and upload new one
                    if (existingCustomer.getPhotoUrl() != null) {
                        fileStorageService.deleteFile(existingCustomer.getPhotoUrl());
                    }

                    String photoUrl = fileStorageService.storeFile(photo, "customer-photos");
                    customer.setPhotoUrl(photoUrl);
                    customer.setPhotoThumbnailUrl(photoUrl); // For simplicity, using same URL
                }

                Customer updatedCustomer = customerService.updateCustomer(id, customer);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Customer " + updatedCustomer.getFirstName() + " " + updatedCustomer.getLastName() + " updated successfully");

                return "redirect:/customers/view/" + id;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
                return "redirect:/customers";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/customers/edit/" + id;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading photo: " + e.getMessage());
            return "redirect:/customers/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating customer: " + e.getMessage());
            return "redirect:/customers/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Customer> customerOpt = customerService.getCustomerById(id);

            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();

                // Delete photo if exists
                if (customer.getPhotoUrl() != null) {
                    fileStorageService.deleteFile(customer.getPhotoUrl());
                }

                boolean deleted = customerService.deleteCustomer(id);

                if (deleted) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Customer " + customer.getFirstName() + " " + customer.getLastName() + " deleted successfully");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete customer");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
            }

            return "redirect:/customers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting customer: " + e.getMessage());
            return "redirect:/customers";
        }
    }

    @PostMapping("/{id}/photo/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Customer> customerOpt = customerService.getCustomerById(id);

            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();

                if (customer.getPhotoUrl() != null) {
                    fileStorageService.deleteFile(customer.getPhotoUrl());

                    // Update customer record to remove photo references
                    customer.setPhotoUrl(null);
                    customer.setPhotoThumbnailUrl(null);
                    customerService.updateCustomer(id, customer);

                    redirectAttributes.addFlashAttribute("successMessage", "Photo deleted successfully");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "No photo to delete");
                }

                return "redirect:/customers/edit/" + id;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Customer not found");
                return "redirect:/customers";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting photo: " + e.getMessage());
            return "redirect:/customers/edit/" + id;
        }
    }

    @GetMapping("/reports")
    public String showReportsPage(Model model) {
        // Generate customer report
        Map<String, Object> reportData = customerService.generateCustomerReport();
        model.addAttribute("reportData", reportData);

        return "customers/reports";
    }
}