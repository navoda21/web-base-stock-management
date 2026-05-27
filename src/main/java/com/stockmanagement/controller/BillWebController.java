package com.stockmanagement.controller;

import com.stockmanagement.dto.PaymentDTO;
import com.stockmanagement.entity.Bill;
import com.stockmanagement.entity.BillItem;
import com.stockmanagement.entity.BillStatus;
import com.stockmanagement.entity.Customer;
import com.stockmanagement.entity.Item;
import com.stockmanagement.repository.ItemRepository;
import com.stockmanagement.service.BillService;
import com.stockmanagement.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/bills")
public class BillWebController {

    @Autowired
    private BillService billService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public String listBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<Bill> bills = billService.getAllBills();

        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            bills = bills.stream()
                    .filter(bill -> {
                        LocalDate billDate = bill.getBillDate().toLocalDate();
                        return !billDate.isBefore(startDate) && !billDate.isAfter(endDate);
                    })
                    .toList();
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        }

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            bills = bills.stream()
                    .filter(bill -> status.equalsIgnoreCase(bill.getStatus().name()))
                    .toList();
            model.addAttribute("status", status);
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min((start + size), bills.size());

        List<Bill> paginatedBills = start < bills.size() ? bills.subList(start, end) : new ArrayList<>();
        Page<Bill> billPage = new PageImpl<>(paginatedBills, PageRequest.of(page, size), bills.size());

        model.addAttribute("bills", billPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", billPage.getTotalPages());

        return "bills/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Get all customers for the dropdown
        List<Customer> customers = customerService.getActiveCustomers();
        model.addAttribute("customers", customers);

        // Get all items for the selection
        List<Item> items = itemRepository.findAll();
        model.addAttribute("products", items);

        // Empty bill for form binding
        model.addAttribute("bill", new Bill());
        model.addAttribute("billItems", new ArrayList<BillItem>());

        return "bills/create";
    }

    @PostMapping("/create")
    public String createBill(@RequestParam Long customerId,
                             @RequestParam(required = false) String notes,
                             @RequestParam("productId") List<Long> productIds,
                             @RequestParam("quantity") List<Integer> quantities,
                             @RequestParam("unitPrice") List<Double> unitPrices,
                             @RequestParam(required = false) String paymentMethod,
                             @RequestParam(required = false) String paymentReference,
                             @RequestParam(required = false) BigDecimal amountPaid,
                             @RequestParam(required = false) String status,
                             RedirectAttributes redirectAttributes) {

        try {
            // Create the bill request DTO
            com.stockmanagement.dto.BillRequest billRequest = com.stockmanagement.dto.BillRequest.create(
                    customerId, productIds, quantities, unitPrices, notes);

            // Create and save the bill
            Bill bill = billService.createBill(billRequest);

            // Set payment information with NULL safety
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                bill.setPaymentMethod(paymentMethod);
            } else {
                bill.setPaymentMethod(null); // Explicitly set to NULL if not provided
            }

            if (paymentReference != null && !paymentReference.trim().isEmpty()) {
                bill.setPaymentReference(paymentReference);
            } else {
                bill.setPaymentReference(null); // Explicitly set to NULL if not provided
            }

            if (amountPaid != null && amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                bill.setAmountPaid(amountPaid);

                // Auto-update status based on amount paid
                if (bill.getTotalAmount() != null) {
                    if (amountPaid.compareTo(bill.getTotalAmount()) >= 0) {
                        bill.setStatus(BillStatus.PAID);
                    } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                        bill.setStatus(BillStatus.PARTIAL);
                    }
                }
            } else {
                bill.setAmountPaid(BigDecimal.ZERO);
            }

            // Set status if provided (overrides auto-calculation)
            if (status != null && !status.trim().isEmpty()) {
                try {
                    bill.setStatus(BillStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    // Keep the auto-calculated status if invalid status provided
                    // Status was already set by createBill or auto-calculation above
                }
            }

            // Save the updated bill with payment info
            bill = billService.saveBill(bill);

            // Update customer loyalty points (1 point for each $1 spent)
            if (bill.getCustomer() != null) {
                int pointsToAdd = bill.getTotalAmount().intValue();
                customerService.updateLoyaltyPoints(customerId, pointsToAdd);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Bill #" + bill.getBillNumber() + " created successfully");

            return "redirect:/bills/view/" + bill.getId();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bills/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating bill: " + e.getMessage());
            return "redirect:/bills/create";
        }
    }

    @GetMapping("/view/{id}")
    public String viewBill(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOpt = billService.getBillById(id);

        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            model.addAttribute("bill", bill);
            model.addAttribute("customer", bill.getCustomer());
            model.addAttribute("items", bill.getItems());

            return "bills/view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
            return "redirect:/bills";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOpt = billService.getBillById(id);

        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            model.addAttribute("bill", bill);

            // Get all customers and items for dropdowns
            List<Customer> customers = customerService.getActiveCustomers();
            model.addAttribute("customers", customers);

            List<Item> items = itemRepository.findAll();
            model.addAttribute("products", items);

            return "bills/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
            return "redirect:/bills";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBill(@PathVariable Long id,
                             @RequestParam Long customerId,
                             @RequestParam(required = false) String notes,
                             @RequestParam("productId") List<Long> productIds,
                             @RequestParam("quantity") List<Integer> quantities,
                             @RequestParam("unitPrice") List<Double> unitPrices,
                             @RequestParam(required = false) String paymentMethod,
                             @RequestParam(required = false) String paymentReference,
                             @RequestParam(required = false) BigDecimal amountPaid,
                             @RequestParam(required = false) String status,
                             RedirectAttributes redirectAttributes) {

        try {
            Bill bill = billService.getBillById(id)
                    .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));

            // Update customer
            Customer customer = customerService.getCustomerById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
            bill.setCustomer(customer);

            // Clear existing items (orphanRemoval=true will handle deletion)
            bill.getItems().clear();

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (int i = 0; i < productIds.size(); i++) {
                Item item = itemRepository.findById(productIds.get(i))
                        .orElseThrow(() -> new RuntimeException("Item not found"));

                BillItem billItem = new BillItem();
                billItem.setBill(bill);
                billItem.setItem(item);
                billItem.setQuantity(quantities.get(i));
                billItem.setUnitPrice(BigDecimal.valueOf(unitPrices.get(i)));
                billItem.setTotalPrice(BigDecimal.valueOf(unitPrices.get(i)).multiply(BigDecimal.valueOf(quantities.get(i))));

                bill.getItems().add(billItem);
                totalAmount = totalAmount.add(billItem.getTotalPrice());
            }

            // Recalculate subtotal and tax
            BigDecimal taxRate = new BigDecimal("0.10");
            BigDecimal subtotal = totalAmount.divide(BigDecimal.ONE.add(taxRate), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal taxAmount = totalAmount.subtract(subtotal);

            bill.setTotalAmount(totalAmount);
            bill.setSubtotalAmount(subtotal);
            bill.setTaxAmount(taxAmount);

            // Update payment information
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                bill.setPaymentMethod(paymentMethod);
            }

            if (paymentReference != null && !paymentReference.trim().isEmpty()) {
                bill.setPaymentReference(paymentReference);
            }

            if (amountPaid != null) {
                bill.setAmountPaid(amountPaid);
            }

            // Update status
            if (status != null && !status.trim().isEmpty()) {
                try {
                    bill.setStatus(BillStatus.valueOf(status));
                } catch (IllegalArgumentException e) {
                    // Keep existing status if invalid
                }
            }

            // Update notes
            bill.setNotes(notes);

            // Save updated bill
            bill = billService.saveBill(bill);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Bill #" + bill.getBillNumber() + " updated successfully");

            return "redirect:/bills/view/" + bill.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating bill: " + e.getMessage());
            return "redirect:/bills/edit/" + id;
        }
    }

    @GetMapping("/print/{id}")
    public String printBill(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOpt = billService.getBillById(id);

        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            model.addAttribute("bill", bill);
            model.addAttribute("customer", bill.getCustomer());
            model.addAttribute("items", bill.getItems());

            return "bills/print";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
            return "redirect:/bills";
        }
    }

    @PostMapping("/{id}/update-status")
    public String updateBillStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BigDecimal amountPaid,
            @RequestParam(required = false) String paymentReference,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Bill> billOpt = billService.getBillById(id);
            if (!billOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
                return "redirect:/bills";
            }

            Bill bill = billOpt.get();

            // Convert string status to enum
            BillStatus status;
            try {
                status = BillStatus.valueOf(paymentStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid bill status: " + paymentStatus);
                return "redirect:/bills/view/" + id;
            }

            // Set payment details if provided
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                bill.setPaymentMethod(paymentMethod);
            }

            if (paymentReference != null && !paymentReference.isEmpty()) {
                bill.setPaymentReference(paymentReference);
            }

            if (amountPaid != null) {
                bill.setAmountPaid(amountPaid);
            }

            // Update the bill status
            bill.setStatus(status);
            Bill updatedBill = billService.updateBillStatus(id, status);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Bill #" + updatedBill.getBillNumber() + " status updated to " + status);

            return "redirect:/bills/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating bill status: " + e.getMessage());
            return "redirect:/bills/view/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Bill> billOpt = billService.getBillById(id);

            if (billOpt.isPresent()) {
                Bill bill = billOpt.get();

                try {
                    billService.deleteBill(id);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Bill #" + bill.getBillNumber() + " deleted successfully");
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete bill: " + ex.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
            }

            return "redirect:/bills";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting bill: " + e.getMessage());
            return "redirect:/bills";
        }
    }

    @GetMapping("/reports")
    public String showReportsPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Default to current month if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Generate bill report
        Map<String, Object> reportData = billService.getSalesReport(startDate, endDate);
        model.addAttribute("reportData", reportData);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "bills/reports";
    }

    @PostMapping("/payment/{id}")
    public String recordPayment(
            @PathVariable Long id,
            @ModelAttribute PaymentDTO paymentDTO,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Bill> billOpt = billService.getBillById(id);
            if (!billOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
                return "redirect:/bills";
            }

            Bill bill = billOpt.get();

            // Check if amount is valid
            if (paymentDTO.getAmount() == null || paymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment amount must be greater than zero");
                return "redirect:/bills/view/" + id;
            }

            // Check if payment method is provided
            if (paymentDTO.getPaymentMethod() == null || paymentDTO.getPaymentMethod().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment method is required");
                return "redirect:/bills/view/" + id;
            }

            // Calculate current amount paid + new payment
            BigDecimal currentAmountPaid = bill.getAmountPaid() != null ? bill.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal totalPaid = currentAmountPaid.add(paymentDTO.getAmount());

            // Determine new bill status
            BillStatus newStatus;
            if (totalPaid.compareTo(bill.getTotalAmount()) >= 0) {
                newStatus = BillStatus.PAID;
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                newStatus = BillStatus.PARTIAL;
            } else {
                newStatus = BillStatus.PENDING;
            }

            // Update bill details
            bill.setAmountPaid(totalPaid);
            bill.setPaymentMethod(paymentDTO.getPaymentMethod());

            if (paymentDTO.getReference() != null && !paymentDTO.getReference().trim().isEmpty()) {
                bill.setPaymentReference(paymentDTO.getReference());
            }

            // Update bill status
            billService.updateBillStatus(id, newStatus);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of $" + paymentDTO.getAmount() + " recorded successfully. Bill status: " + newStatus);

            return "redirect:/bills/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording payment: " + e.getMessage());
            return "redirect:/bills/view/" + id;
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelBill(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Bill> billOpt = billService.getBillById(id);
            if (!billOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bill not found");
                return "redirect:/bills";
            }

            Bill bill = billOpt.get();

            // Cannot cancel a bill that's already paid
            if (bill.getStatus() == BillStatus.PAID) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot cancel a paid bill. Process a refund instead.");
                return "redirect:/bills/view/" + id;
            }

            // Store the reason as a note
            if (reason != null && !reason.trim().isEmpty()) {
                String currentNotes = bill.getNotes() != null ? bill.getNotes() : "";
                bill.setNotes(currentNotes + "\n[CANCELLED] " + reason);
            }

            // Update bill status
            Bill updatedBill = billService.updateBillStatus(id, BillStatus.CANCELLED);

            // Restore the stock for all items in the bill
            for (BillItem item : bill.getItems()) {
                if (item.getItem() != null && item.getQuantity() != null) {
                    Item inventoryItem = item.getItem();
                    inventoryItem.setQuantity(inventoryItem.getQuantity() + item.getQuantity());
                    itemRepository.save(inventoryItem);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Bill #" + updatedBill.getBillNumber() + " has been cancelled.");

            return "redirect:/bills/view/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling bill: " + e.getMessage());
            return "redirect:/bills/view/" + id;
        }
    }
}