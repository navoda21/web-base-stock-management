package com.stockmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle navigation to the Customer & Bills section
 */
@Controller
public class CustomerBillsController {

    /**
     * Redirects users who click on "Customer & Bills" link to the bills page
     * since we're enabling this functionality now
     */
    @GetMapping("/customer-bills")
    public String redirectToCustomerBills() {
        return "redirect:/bills";
    }
}