package com.stockmanagement.controller;

import com.stockmanagement.entity.Staff;
import com.stockmanagement.service.FileStorageService;
import com.stockmanagement.service.StaffService;
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
import java.util.Optional;

@Controller
@RequestMapping("/staff")
public class StaffWebController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String listStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<Staff> allStaff = staffService.getAllStaff();

        // Manual pagination since we're not using Spring Data repositories directly
        int start = page * size;
        int end = Math.min((start + size), allStaff.size());

        List<Staff> paginatedStaff = allStaff.subList(start, end);
        Page<Staff> staffPage = new PageImpl<>(paginatedStaff, PageRequest.of(page, size), allStaff.size());

        model.addAttribute("staffList", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());

        return "staff/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("staff", new Staff());
        return "staff/create";
    }

    @PostMapping("/create")
    public String createStaff(
            @ModelAttribute Staff staff,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        try {
            // Handle photo upload if provided
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = fileStorageService.storeFile(photo, "staff-photos");
                staff.setPhotoUrl(photoUrl);
                staff.setPhotoThumbnailUrl(photoUrl); // For simplicity, using same URL
            }

            Staff savedStaff = staffService.createStaff(staff);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Staff member " + savedStaff.getFirstName() + " " + savedStaff.getLastName() + " created successfully");

            return "redirect:/staff";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("staff", staff);
            return "redirect:/staff/create";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading photo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("staff", staff);
            return "redirect:/staff/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating staff: " + e.getMessage());
            redirectAttributes.addFlashAttribute("staff", staff);
            return "redirect:/staff/create";
        }
    }

    @GetMapping("/view/{id}")
    public String viewStaff(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Staff> staffOpt = staffService.getStaffById(id);

        if (staffOpt.isPresent()) {
            model.addAttribute("staff", staffOpt.get());
            return "staff/view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Staff member not found");
            return "redirect:/staff";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Staff> staffOpt = staffService.getStaffById(id);

        if (staffOpt.isPresent()) {
            model.addAttribute("staff", staffOpt.get());
            return "staff/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Staff member not found");
            return "redirect:/staff";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateStaff(
            @PathVariable Long id,
            @ModelAttribute Staff staff,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        try {
            // Get existing staff to preserve photo URL if no new photo
            Optional<Staff> existingStaffOpt = staffService.getStaffById(id);

            if (existingStaffOpt.isPresent()) {
                Staff existingStaff = existingStaffOpt.get();

                // If no new photo is uploaded, keep the existing photo
                if ((photo == null || photo.isEmpty()) && existingStaff.getPhotoUrl() != null) {
                    staff.setPhotoUrl(existingStaff.getPhotoUrl());
                    staff.setPhotoThumbnailUrl(existingStaff.getPhotoThumbnailUrl());
                } else if (photo != null && !photo.isEmpty()) {
                    // If a new photo is uploaded, delete old photo and upload new one
                    if (existingStaff.getPhotoUrl() != null) {
                        fileStorageService.deleteFile(existingStaff.getPhotoUrl());
                    }

                    String photoUrl = fileStorageService.storeFile(photo, "staff-photos");
                    staff.setPhotoUrl(photoUrl);
                    staff.setPhotoThumbnailUrl(photoUrl); // For simplicity, using same URL
                }

                Staff updatedStaff = staffService.updateStaff(id, staff);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Staff member " + updatedStaff.getFirstName() + " " + updatedStaff.getLastName() + " updated successfully");

                return "redirect:/staff/view/" + id;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Staff member not found");
                return "redirect:/staff";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/staff/edit/" + id;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading photo: " + e.getMessage());
            return "redirect:/staff/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating staff: " + e.getMessage());
            return "redirect:/staff/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Staff> staffOpt = staffService.getStaffById(id);

            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();

                // Delete photo if exists
                if (staff.getPhotoUrl() != null) {
                    fileStorageService.deleteFile(staff.getPhotoUrl());
                }

                boolean deleted = staffService.deleteStaff(id);

                if (deleted) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Staff member " + staff.getFirstName() + " " + staff.getLastName() + " deleted successfully");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete staff member");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Staff member not found");
            }

            return "redirect:/staff";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting staff: " + e.getMessage());
            return "redirect:/staff";
        }
    }

    @PostMapping("/{id}/photo/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Staff> staffOpt = staffService.getStaffById(id);

            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();

                if (staff.getPhotoUrl() != null) {
                    fileStorageService.deleteFile(staff.getPhotoUrl());

                    // Update staff record to remove photo references
                    staff.setPhotoUrl(null);
                    staff.setPhotoThumbnailUrl(null);
                    staffService.updateStaff(id, staff);

                    redirectAttributes.addFlashAttribute("successMessage", "Photo deleted successfully");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "No photo to delete");
                }

                return "redirect:/staff/edit/" + id;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Staff member not found");
                return "redirect:/staff";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting photo: " + e.getMessage());
            return "redirect:/staff/edit/" + id;
        }
    }

    @GetMapping("/search")
    public String advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String hireDateFrom,
            @RequestParam(required = false) String hireDateTo,
            @RequestParam(required = false) Boolean isActive,
            Model model) {

        try {
            List<Staff> staffList = staffService.advancedSearch(
                    name, department, role, minSalary, maxSalary,
                    minRating, maxRating, hireDateFrom, hireDateTo, isActive);

            model.addAttribute("staffList", staffList);

            // Add search parameters back to model for form persistence
            model.addAttribute("searchName", name);
            model.addAttribute("searchDepartment", department);
            model.addAttribute("searchRole", role);
            model.addAttribute("searchMinSalary", minSalary);
            model.addAttribute("searchMaxSalary", maxSalary);
            model.addAttribute("searchMinRating", minRating);
            model.addAttribute("searchMaxRating", maxRating);
            model.addAttribute("searchHireDateFrom", hireDateFrom);
            model.addAttribute("searchHireDateTo", hireDateTo);
            model.addAttribute("searchIsActive", isActive);

            return "staff/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error performing search: " + e.getMessage());
            return "staff/list";
        }
    }

    @GetMapping("/reports")
    public String showReportsPage(Model model) {
        // Initialize with default report data (all staff, all time)
        model.addAttribute("reportData",
                staffService.generateStaffReport(null, null, null));

        return "staff/reports";
    }

    @GetMapping("/reports/generate")
    public String generateReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String department,
            Model model) {

        try {
            model.addAttribute("reportData",
                    staffService.generateStaffReport(startDate, endDate, department));

            // Add parameters back to model for form persistence
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("department", department);

            return "staff/reports";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error generating report: " + e.getMessage());
            return "staff/reports";
        }
    }
}