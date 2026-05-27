package com.stockmanagement.controller;

import com.stockmanagement.entity.Staff;
import com.stockmanagement.service.FileStorageService;
import com.stockmanagement.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<Staff>> getAllStaff() {
        try {
            List<Staff> staffList = staffService.getAllStaff();
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Staff>> getActiveStaff() {
        try {
            List<Staff> staffList = staffService.getActiveStaff();
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable Long id) {
        try {
            Optional<Staff> staff = staffService.getStaffById(id);
            if (staff.isPresent()) {
                return ResponseEntity.ok(staff.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving staff: " + e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getStaffByEmployeeId(@PathVariable String employeeId) {
        try {
            Optional<Staff> staff = staffService.getStaffByEmployeeId(employeeId);
            if (staff.isPresent()) {
                return ResponseEntity.ok(staff.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff not found with Employee ID: " + employeeId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving staff: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createStaff(@RequestBody Staff staff) {
        try {
            // Validate required fields
            if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("First name is required"));
            }

            if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Last name is required"));
            }

            if (staff.getEmail() == null || staff.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Email is required"));
            }

            if (staff.getRole() == null || staff.getRole().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Role is required"));
            }

            if (staff.getHireDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Hire date is required"));
            }

            // Check if email already exists
            Optional<Staff> existingByEmail = staffService.getStaffByEmail(staff.getEmail());
            if (existingByEmail.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Email already exists"));
            }

            // Check if employee ID already exists (if provided)
            if (staff.getEmployeeId() != null && !staff.getEmployeeId().trim().isEmpty()) {
                Optional<Staff> existingByEmployeeId = staffService.getStaffByEmployeeId(staff.getEmployeeId());
                if (existingByEmployeeId.isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Staff ID already exists"));
                }
            }

            Staff savedStaff = staffService.createStaff(staff);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStaff);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating staff: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody Staff staffDetails) {
        try {
            // Validate required fields
            if (staffDetails.getFirstName() == null || staffDetails.getFirstName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("First name is required"));
            }

            if (staffDetails.getLastName() == null || staffDetails.getLastName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Last name is required"));
            }

            if (staffDetails.getEmail() == null || staffDetails.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Email is required"));
            }

            if (staffDetails.getRole() == null || staffDetails.getRole().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Role is required"));
            }

            // Check if staff exists
            Optional<Staff> existingStaff = staffService.getStaffById(id);
            if (!existingStaff.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff ID not found"));
            }

            // Check if email is being changed and if new email already exists
            Staff currentStaff = existingStaff.get();
            if (!currentStaff.getEmail().equals(staffDetails.getEmail())) {
                Optional<Staff> existingByEmail = staffService.getStaffByEmail(staffDetails.getEmail());
                if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createErrorResponse("Email already exists"));
                }
            }

            Staff updatedStaff = staffService.updateStaff(id, staffDetails);
            return ResponseEntity.ok(updatedStaff);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating staff: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            // Check if staff exists
            Optional<Staff> existingStaff = staffService.getStaffById(id);
            if (!existingStaff.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff ID not found"));
            }

            boolean deleted = staffService.deleteStaff(id);
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Staff permanently deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error deleting staff"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting staff: " + e.getMessage()));
        }
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<?> getStaffByDepartment(@PathVariable String department) {
        try {
            List<Staff> staffList = staffService.getStaffByDepartment(department);
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving staff by department: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/performance")
    public ResponseEntity<?> updatePerformanceRating(@PathVariable Long id, @RequestParam Double rating) {
        try {
            // Validate rating
            if (rating == null || rating < 1 || rating > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Performance rating must be between 1 and 5"));
            }

            // Check if staff exists
            Optional<Staff> existingStaff = staffService.getStaffById(id);
            if (!existingStaff.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff ID not found"));
            }

            Staff updatedStaff = staffService.updatePerformanceRating(id, rating);
            return ResponseEntity.ok(updatedStaff);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating performance rating: " + e.getMessage()));
        }
    }

    // Photo upload endpoint
    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadStaffPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) {
        try {
            if (photo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Photo file is required"));
            }

            // Validate file type
            String contentType = photo.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/jpeg") &&
                            !contentType.startsWith("image/png") &&
                            !contentType.startsWith("image/gif"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Only JPEG, PNG, and GIF images are allowed"));
            }

            // Note: Max file size is now configured in application.properties
            // This is a secondary check in case the MultipartException isn't thrown
            if (photo.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("File size must be less than 5MB"));
            }

            Optional<Staff> existingStaff = staffService.getStaffById(id);
            if (!existingStaff.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff not found"));
            }

            // Delete old photo if exists
            Staff staff = existingStaff.get();
            if (staff.getPhotoUrl() != null) {
                fileStorageService.deleteFile(staff.getPhotoUrl());
            }

            // Store new photo
            String photoUrl = fileStorageService.storeFile(photo, "staff-photos");

            // For simplicity, using same URL for thumbnail
            staff.setPhotoUrl(photoUrl);
            staff.setPhotoThumbnailUrl(photoUrl);

            Staff updatedStaff = staffService.updateStaff(id, staff);
            return ResponseEntity.ok(updatedStaff);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error uploading photo: " + e.getMessage()));
        }
    }

    // Photo delete endpoint
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<?> deleteStaffPhoto(@PathVariable Long id) {
        try {
            Optional<Staff> existingStaff = staffService.getStaffById(id);
            if (!existingStaff.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Staff not found"));
            }

            Staff staff = existingStaff.get();
            if (staff.getPhotoUrl() != null) {
                // Delete the physical file
                fileStorageService.deleteFile(staff.getPhotoUrl());

                // Update the staff record
                staff.setPhotoUrl(null);
                staff.setPhotoThumbnailUrl(null);
                staffService.updateStaff(id, staff);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Photo deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting photo: " + e.getMessage()));
        }
    }

    // Advanced search endpoint
    @GetMapping("/search/advanced")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) String hireDateFrom,
            @RequestParam(required = false) String hireDateTo,
            @RequestParam(required = false) Boolean isActive) {

        try {
            List<Staff> staffList = staffService.advancedSearch(
                    name, department, role, minSalary, maxSalary,
                    minRating, maxRating, hireDateFrom, hireDateTo, isActive
            );
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error performing advanced search: " + e.getMessage()));
        }
    }

    // Reporting endpoint
    @GetMapping("/reports/summary")
    public ResponseEntity<?> getStaffReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String department) {

        try {
            Map<String, Object> report = staffService.generateStaffReport(startDate, endDate, department);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error generating report: " + e.getMessage()));
        }
    }

    // Helper method to create consistent error responses
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}