package com.stockmanagement.service;

import com.stockmanagement.entity.Staff;
import com.stockmanagement.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Phone validation pattern (exactly 10 digits)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10}$");

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }

    public Optional<Staff> getStaffByEmployeeId(String employeeId) {
        return staffRepository.findByEmployeeId(employeeId);
    }

    public Optional<Staff> getStaffByEmail(String email) {
        return staffRepository.findByEmail(email.toLowerCase()); // Always search with lowercase
    }

    public Staff createStaff(Staff staff) {
        // Format data before validation
        formatStaffData(staff);

        // Validate all fields comprehensively
        validateStaffData(staff, false);

        // Generate employee ID if not provided
        if (staff.getEmployeeId() == null || staff.getEmployeeId().trim().isEmpty()) {
            String newEmployeeId = generateEmployeeId();
            staff.setEmployeeId(newEmployeeId);
        } else {
            // Validate provided employee ID format
            if (!isValidEmployeeId(staff.getEmployeeId())) {
                throw new IllegalArgumentException("Invalid employee ID format. Must be like EMP001");
            }

            // Check if provided employee ID already exists
            Optional<Staff> existingStaff = staffRepository.findByEmployeeId(staff.getEmployeeId());
            if (existingStaff.isPresent()) {
                throw new IllegalArgumentException("Employee ID already exists");
            }
        }

        // Check if email already exists (additional safety check)
        Optional<Staff> existingByEmail = staffRepository.findByEmail(staff.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Set default values
        if (staff.getIsActive() == null) {
            staff.setIsActive(true);
        }

        return staffRepository.save(staff);
    }

    public Staff updateStaff(Long id, Staff staffDetails) {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (optionalStaff.isPresent()) {
            Staff staff = optionalStaff.get();

            // Format data before validation
            formatStaffData(staffDetails);

            // Validate all fields for update
            validateStaffData(staffDetails, true);

            // Check if email is being changed and if new email already exists
            if (!staff.getEmail().equals(staffDetails.getEmail())) {
                Optional<Staff> existingByEmail = staffRepository.findByEmail(staffDetails.getEmail());
                if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }

            // Update all fields
            staff.setFirstName(staffDetails.getFirstName());
            staff.setLastName(staffDetails.getLastName());
            staff.setEmail(staffDetails.getEmail());
            staff.setPhone(staffDetails.getPhone());
            staff.setRole(staffDetails.getRole());
            staff.setDepartment(staffDetails.getDepartment());
            staff.setHireDate(staffDetails.getHireDate());
            staff.setSalary(staffDetails.getSalary());
            staff.setShiftSchedule(staffDetails.getShiftSchedule());
            staff.setPerformanceRating(staffDetails.getPerformanceRating());
            staff.setIsActive(staffDetails.getIsActive());

            // Update photo fields if provided
            if (staffDetails.getPhotoUrl() != null) {
                staff.setPhotoUrl(staffDetails.getPhotoUrl());
            }
            if (staffDetails.getPhotoThumbnailUrl() != null) {
                staff.setPhotoThumbnailUrl(staffDetails.getPhotoThumbnailUrl());
            }

            return staffRepository.save(staff);
        }
        throw new IllegalArgumentException("Staff not found with ID: " + id);
    }

    // NEW: Format staff data before validation and saving
    private void formatStaffData(Staff staff) {
        // Capitalize first letter of each word in names
        if (staff.getFirstName() != null) {
            staff.setFirstName(capitalizeName(staff.getFirstName()));
        }
        if (staff.getLastName() != null) {
            staff.setLastName(capitalizeName(staff.getLastName()));
        }

        // Convert email to lowercase
        if (staff.getEmail() != null) {
            staff.setEmail(staff.getEmail().toLowerCase().trim());
        }

        // Capitalize department if provided
        if (staff.getDepartment() != null && !staff.getDepartment().trim().isEmpty()) {
            staff.setDepartment(capitalizeName(staff.getDepartment()));
        }

        // Capitalize role if provided
        if (staff.getRole() != null && !staff.getRole().trim().isEmpty()) {
            staff.setRole(capitalizeName(staff.getRole()));
        }

        // Clean phone number - remove all non-digit characters
        if (staff.getPhone() != null && !staff.getPhone().trim().isEmpty()) {
            staff.setPhone(staff.getPhone().replaceAll("\\D", ""));
        }
    }

    // NEW: Capitalize first letter of each word in a string
    private String capitalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        String trimmedName = name.trim();
        String[] words = trimmedName.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                if (capitalized.length() > 0) {
                    capitalized.append(" ");
                }
                // Capitalize first letter and make rest lowercase
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
        }

        return capitalized.toString();
    }

    // Comprehensive validation method
    private void validateStaffData(Staff staff, boolean isUpdate) {
        // Required field validations
        if (staff.getFirstName() == null || staff.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (staff.getFirstName().length() < 2 || staff.getFirstName().length() > 50) {
            throw new IllegalArgumentException("First name must be between 2 and 50 characters");
        }

        if (staff.getLastName() == null || staff.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (staff.getLastName().length() < 2 || staff.getLastName().length() > 50) {
            throw new IllegalArgumentException("Last name must be between 2 and 50 characters");
        }

        if (staff.getEmail() == null || staff.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(staff.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (staff.getEmail().length() > 100) {
            throw new IllegalArgumentException("Email must be less than 100 characters");
        }

        // Phone validation (optional but must be valid if provided)
        if (staff.getPhone() != null && !staff.getPhone().trim().isEmpty()) {
            if (!isValidPhone(staff.getPhone())) {
                throw new IllegalArgumentException("Phone number must be exactly 10 digits");
            }
        }

        if (staff.getRole() == null || staff.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        if (staff.getRole().length() > 50) {
            throw new IllegalArgumentException("Role must be less than 50 characters");
        }

        if (staff.getHireDate() == null) {
            throw new IllegalArgumentException("Hire date is required");
        }
        if (staff.getHireDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hire date cannot be in the future");
        }
        if (staff.getHireDate().isBefore(LocalDate.of(2000, 1, 1))) {
            throw new IllegalArgumentException("Hire date cannot be before year 2000");
        }

        // Department validation
        if (staff.getDepartment() != null && staff.getDepartment().length() > 50) {
            throw new IllegalArgumentException("Department must be less than 50 characters");
        }

        // Salary validation
        if (staff.getSalary() != null) {
            if (staff.getSalary() < 0) {
                throw new IllegalArgumentException("Salary cannot be negative");
            }
            if (staff.getSalary() > 1000000) {
                throw new IllegalArgumentException("Salary cannot exceed 1,000,000 LKR");
            }
        }

        // Performance rating validation
        if (staff.getPerformanceRating() != null) {
            if (staff.getPerformanceRating() < 1 || staff.getPerformanceRating() > 5) {
                throw new IllegalArgumentException("Performance rating must be between 1 and 5");
            }
        }

        // Employee ID validation (only for create)
        if (!isUpdate && staff.getEmployeeId() != null && !staff.getEmployeeId().trim().isEmpty()) {
            if (!isValidEmployeeId(staff.getEmployeeId())) {
                throw new IllegalArgumentException("Invalid employee ID format. Must be like EMP001");
            }
        }
    }

    public boolean deleteStaff(Long id) {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (optionalStaff.isPresent()) {
            staffRepository.hardDeleteById(id);
            return true;
        }
        return false;
    }

    public List<Staff> getStaffByDepartment(String department) {
        return staffRepository.findByDepartment(department);
    }

    public Staff updatePerformanceRating(Long id, Double rating) {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (optionalStaff.isPresent()) {
            // Validate performance rating
            if (rating == null) {
                throw new IllegalArgumentException("Performance rating is required");
            }
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Performance rating must be between 1 and 5");
            }

            Staff staff = optionalStaff.get();
            staff.setPerformanceRating(rating);
            return staffRepository.save(staff);
        }
        throw new IllegalArgumentException("Staff not found with ID: " + id);
    }

    // Advanced search method
    public List<Staff> advancedSearch(String name, String department, String role,
                                      Double minSalary, Double maxSalary,
                                      Double minRating, Double maxRating,
                                      String hireDateFrom, String hireDateTo,
                                      Boolean isActive) {

        List<Staff> allStaff = staffRepository.findAll();

        return allStaff.stream()
                .filter(staff -> name == null || name.isEmpty() ||
                        (staff.getFirstName() + " " + staff.getLastName()).toLowerCase().contains(name.toLowerCase()) ||
                        staff.getEmail().toLowerCase().contains(name.toLowerCase()))
                .filter(staff -> department == null || department.isEmpty() ||
                        (staff.getDepartment() != null && staff.getDepartment().equalsIgnoreCase(department)))
                .filter(staff -> role == null || role.isEmpty() ||
                        staff.getRole().equalsIgnoreCase(role))
                .filter(staff -> minSalary == null ||
                        (staff.getSalary() != null && staff.getSalary() >= minSalary))
                .filter(staff -> maxSalary == null ||
                        (staff.getSalary() != null && staff.getSalary() <= maxSalary))
                .filter(staff -> minRating == null ||
                        (staff.getPerformanceRating() != null && staff.getPerformanceRating() >= minRating))
                .filter(staff -> maxRating == null ||
                        (staff.getPerformanceRating() != null && staff.getPerformanceRating() <= maxRating))
                .filter(staff -> hireDateFrom == null || hireDateFrom.isEmpty() ||
                        staff.getHireDate().isAfter(LocalDate.parse(hireDateFrom).minusDays(1)))
                .filter(staff -> hireDateTo == null || hireDateTo.isEmpty() ||
                        staff.getHireDate().isBefore(LocalDate.parse(hireDateTo).plusDays(1)))
                .filter(staff -> isActive == null || staff.getIsActive().equals(isActive))
                .collect(Collectors.toList());
    }

    // Report generation method
    public Map<String, Object> generateStaffReport(String startDate, String endDate, String department) {
        Map<String, Object> report = new HashMap<>();
        List<Staff> staffList = staffRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate, formatter) : LocalDate.of(2000, 1, 1);
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate, formatter) : LocalDate.now();

        // Filter staff based on criteria
        List<Staff> filteredStaff = staffList.stream()
                .filter(staff -> staff.getHireDate().isAfter(start.minusDays(1)) &&
                        staff.getHireDate().isBefore(end.plusDays(1)))
                .filter(staff -> department == null || department.isEmpty() ||
                        (staff.getDepartment() != null && staff.getDepartment().equals(department)))
                .collect(Collectors.toList());

        // Report data
        report.put("totalStaff", filteredStaff.size());
        report.put("activeStaff", filteredStaff.stream().filter(Staff::getIsActive).count());
        report.put("averageSalary", filteredStaff.stream()
                .filter(staff -> staff.getSalary() != null)
                .mapToDouble(Staff::getSalary)
                .average()
                .orElse(0.0));
        report.put("averageRating", filteredStaff.stream()
                .filter(staff -> staff.getPerformanceRating() != null)
                .mapToDouble(Staff::getPerformanceRating)
                .average()
                .orElse(0.0));

        // Department breakdown
        Map<String, Long> departmentCount = filteredStaff.stream()
                .filter(staff -> staff.getDepartment() != null)
                .collect(Collectors.groupingBy(Staff::getDepartment, Collectors.counting()));
        report.put("departmentBreakdown", departmentCount);

        // Role breakdown
        Map<String, Long> roleCount = filteredStaff.stream()
                .collect(Collectors.groupingBy(Staff::getRole, Collectors.counting()));
        report.put("roleBreakdown", roleCount);

        report.put("reportPeriod",
                (startDate != null ? startDate : "2000-01-01") + " to " +
                        (endDate != null ? endDate : LocalDate.now().toString()));
        report.put("generatedAt", LocalDate.now().toString());

        return report;
    }

    // Improved employee ID generation to avoid duplicates
    private String generateEmployeeId() {
        // Get the maximum employee ID from the database
        List<Staff> allStaff = staffRepository.findAll();

        if (allStaff.isEmpty()) {
            return "EMP001";
        }

        // Find the highest employee ID number
        int maxId = allStaff.stream()
                .map(Staff::getEmployeeId)
                .filter(empId -> empId != null && empId.matches("^EMP\\d+$"))
                .map(empId -> {
                    try {
                        return Integer.parseInt(empId.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("EMP%03d", maxId + 1);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Remove any spaces, dashes, or parentheses
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return cleanPhone != null && PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    private boolean isValidEmployeeId(String employeeId) {
        return employeeId != null && employeeId.matches("^EMP\\d{3,}$");
    }

    // Additional utility methods
    public long getTotalStaffCount() {
        return staffRepository.count();
    }

    public long getActiveStaffCount() {
        return staffRepository.findByIsActiveTrue().size();
    }

    public List<Staff> getStaffHiredAfter(LocalDate date) {
        return staffRepository.findByHireDateAfterAndIsActiveTrue(date);
    }

    public List<Staff> getHighPerformers(double minRating) {
        return staffRepository.findByPerformanceRatingGreaterThanEqualAndIsActiveTrue(minRating);
    }

    public List<Staff> getActiveStaff() {
        return staffRepository.findByIsActiveTrue();
    }
}