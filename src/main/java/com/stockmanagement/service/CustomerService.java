package com.stockmanagement.service;

import com.stockmanagement.entity.Customer;
import com.stockmanagement.factory.MembershipFactory;
import com.stockmanagement.factory.MembershipLevel;
import com.stockmanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MembershipFactory membershipFactory;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Phone validation pattern (exactly 10 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> getActiveCustomers() {
        return customerRepository.findByIsActiveTrue();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email.toLowerCase()); // Always search with lowercase
    }

    public Optional<Customer> getCustomerByCustomerId(String customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    public Customer createCustomer(Customer customer) {
        // Format data
        formatCustomerData(customer);

        // Validate customer data
        validateCustomerData(customer, false);

        // Generate customer ID if not provided
        if (customer.getCustomerId() == null || customer.getCustomerId().trim().isEmpty()) {
            String newCustomerId = generateCustomerId();
            customer.setCustomerId(newCustomerId);
        }

        // Check if email already exists
        Optional<Customer> existingByEmail = customerRepository.findByEmail(customer.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Set default values
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }

        if (customer.getLoyaltyPoints() == null) {
            customer.setLoyaltyPoints(0);
        }

        if (customer.getMembershipLevel() == null || customer.getMembershipLevel().isEmpty()) {
            customer.setMembershipLevel("Standard");
        }

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer customerDetails) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();

            // Format data
            formatCustomerData(customerDetails);

            // Validate customer data
            validateCustomerData(customerDetails, true);

            // Check if email is being changed and if new email already exists
            if (!customer.getEmail().equals(customerDetails.getEmail())) {
                Optional<Customer> existingByEmail = customerRepository.findByEmail(customerDetails.getEmail());
                if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }

            // Update basic fields
            customer.setFirstName(customerDetails.getFirstName());
            customer.setLastName(customerDetails.getLastName());
            customer.setEmail(customerDetails.getEmail());
            customer.setPhone(customerDetails.getPhone());

            // Update additional fields
            if (customerDetails.getAddress() != null) {
                customer.setAddress(customerDetails.getAddress());
            }

            if (customerDetails.getCity() != null) {
                customer.setCity(customerDetails.getCity());
            }

            if (customerDetails.getPostalCode() != null) {
                customer.setPostalCode(customerDetails.getPostalCode());
            }

            if (customerDetails.getCountry() != null) {
                customer.setCountry(customerDetails.getCountry());
            }

            if (customerDetails.getLoyaltyPoints() != null) {
                customer.setLoyaltyPoints(customerDetails.getLoyaltyPoints());
            }

            if (customerDetails.getMembershipLevel() != null) {
                customer.setMembershipLevel(customerDetails.getMembershipLevel());
            }

            if (customerDetails.getNotes() != null) {
                customer.setNotes(customerDetails.getNotes());
            }

            if (customerDetails.getIsActive() != null) {
                customer.setIsActive(customerDetails.getIsActive());
            }

            // Update photo fields if provided
            if (customerDetails.getPhotoUrl() != null) {
                customer.setPhotoUrl(customerDetails.getPhotoUrl());
            }

            if (customerDetails.getPhotoThumbnailUrl() != null) {
                customer.setPhotoThumbnailUrl(customerDetails.getPhotoThumbnailUrl());
            }

            return customerRepository.save(customer);
        }
        throw new IllegalArgumentException("Customer not found with ID: " + id);
    }

    public boolean deleteCustomer(Long id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            customerRepository.hardDeleteById(id);
            return true;
        }
        return false;
    }

    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.searchCustomers(keyword);
    }

    // Method to update loyalty points
    public Customer updateLoyaltyPoints(Long id, Integer points) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            Integer currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            customer.setLoyaltyPoints(currentPoints + points);

            // Update membership level based on loyalty points
            updateMembershipLevel(customer);

            return customerRepository.save(customer);
        }
        throw new IllegalArgumentException("Customer not found with ID: " + id);
    }

    // Method to update membership level based on loyalty points

    /**
     * Update customer's membership level based on loyalty points
     * REFACTORED: Now using Factory Pattern instead of if-else chains
     *
     * @param customer Customer to update
     */
    public void updateMembershipLevel(Customer customer) {
        Integer points = customer.getLoyaltyPoints();
        if (points == null) {
            points = 0;
        }

        // Use Factory Pattern to determine membership level based on points
        // Convert points to spending equivalent (simplified: 1 point = $1)
        double equivalentSpending = points.doubleValue();

        // Get appropriate membership level using factory
        MembershipLevel membershipLevel = membershipFactory.createMembershipBySpending(equivalentSpending);

        // Set the membership level
        customer.setMembershipLevel(membershipLevel.getLevelName());

        // Log the membership level details
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("🏆 Membership Level Updated - Factory Pattern");
        System.out.println("───────────────────────────────────────────────");
        System.out.println("Customer: " + customer.getFirstName() + " " + customer.getLastName());
        System.out.println("Points: " + points);
        System.out.println("New Level: " + membershipLevel.getLevelName());
        System.out.println("Discount: " + membershipLevel.getDiscountPercentage() + "%");
        System.out.println("Points Multiplier: " + membershipLevel.getPointsMultiplier() + "x");
        System.out.println("Benefits: " + membershipLevel.getBenefitsDescription());

        // Check if eligible for upgrade
        if (membershipLevel.isEligibleForUpgrade(equivalentSpending)) {
            System.out.println("🎯 Eligible for upgrade to: " + membershipLevel.getNextLevelName());
            double pointsNeeded = membershipFactory.createMembership(membershipLevel.getNextLevelName())
                    .getMinimumSpending() - equivalentSpending;
            System.out.println("   Points needed: " + (int) pointsNeeded);
        }
        System.out.println("═══════════════════════════════════════════════");
    }

    // Helper method to format customer data
    private void formatCustomerData(Customer customer) {
        // Capitalize first letter of each word in names
        if (customer.getFirstName() != null) {
            customer.setFirstName(capitalizeName(customer.getFirstName()));
        }

        if (customer.getLastName() != null) {
            customer.setLastName(capitalizeName(customer.getLastName()));
        }

        // Convert email to lowercase
        if (customer.getEmail() != null) {
            customer.setEmail(customer.getEmail().toLowerCase().trim());
        }

        // Clean phone number - remove all non-digit characters
        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            customer.setPhone(customer.getPhone().replaceAll("\\D", ""));
        }

        // Capitalize city and country if provided
        if (customer.getCity() != null && !customer.getCity().trim().isEmpty()) {
            customer.setCity(capitalizeName(customer.getCity()));
        }

        if (customer.getCountry() != null && !customer.getCountry().trim().isEmpty()) {
            customer.setCountry(capitalizeName(customer.getCountry()));
        }
    }

    // Helper method to capitalize first letter of each word in a string
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
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
        }

        return capitalized.toString();
    }

    // Validation method
    private void validateCustomerData(Customer customer, boolean isUpdate) {
        // Required field validations
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (!isValidPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Phone number must be exactly 10 digits");
        }
    }

    // Email validation
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Phone validation
    private boolean isValidPhone(String phone) {
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return cleanPhone != null && PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    // Method to generate a unique customer ID
    private String generateCustomerId() {
        // Get the maximum customer ID from the database
        List<Customer> allCustomers = customerRepository.findAll();

        if (allCustomers.isEmpty()) {
            return "CUST001";
        }

        // Find the highest customer ID number
        int maxId = allCustomers.stream()
                .map(Customer::getCustomerId)
                .filter(custId -> custId != null && custId.matches("^CUST\\d+$"))
                .map(custId -> {
                    try {
                        return Integer.parseInt(custId.substring(4));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("CUST%03d", maxId + 1);
    }

    // Report generation method
    public Map<String, Object> generateCustomerReport() {
        Map<String, Object> report = new HashMap<>();
        List<Customer> customers = customerRepository.findAll();

        // Basic metrics
        report.put("totalCustomers", customers.size());
        report.put("activeCustomers", customers.stream().filter(Customer::getIsActive).count());

        // Membership breakdown
        Map<String, Long> membershipCount = customers.stream()
                .filter(c -> c.getMembershipLevel() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        Customer::getMembershipLevel, java.util.stream.Collectors.counting()));
        report.put("membershipBreakdown", membershipCount);

        // Loyalty points stats
        double averagePoints = customers.stream()
                .filter(c -> c.getLoyaltyPoints() != null)
                .mapToInt(Customer::getLoyaltyPoints)
                .average()
                .orElse(0.0);
        report.put("averageLoyaltyPoints", averagePoints);

        report.put("generatedAt", LocalDateTime.now());

        return report;
    }
}

