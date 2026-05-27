// AdminController.java
package com.stockmanagement.controller;

import com.stockmanagement.entity.UserRole;
import com.stockmanagement.service.StaffService;
import com.stockmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private StaffService staffService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Dashboard statistics
        long totalUsers = userService.getTotalActiveUsers();
        long adminCount = userService.getUserCountByRole(UserRole.ADMIN);
        long stockManagerCount = userService.getUserCountByRole(UserRole.STOCK_MANAGER);
        long salesStaffCount = userService.getUserCountByRole(UserRole.SALES_STAFF);
        long hrStaffCount = userService.getUserCountByRole(UserRole.HR_STAFF);

        // Staff statistics
        long totalStaffCount = staffService.getTotalStaffCount();
        long activeStaffCount = staffService.getActiveStaffCount();

        model.addAttribute("currentUser", authentication.getName());
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("stockManagerCount", stockManagerCount);
        model.addAttribute("salesStaffCount", salesStaffCount);
        model.addAttribute("hrStaffCount", hrStaffCount);
        model.addAttribute("totalStaffCount", totalStaffCount);
        model.addAttribute("activeStaffCount", activeStaffCount);

        return "admin/dashboard";
    }
}
