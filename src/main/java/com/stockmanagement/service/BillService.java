package com.stockmanagement.service;

import com.stockmanagement.entity.*;
import com.stockmanagement.repository.BillRepository;
import com.stockmanagement.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    // Getters and Setters for dependencies
    public BillRepository getBillRepository() {
        return billRepository;
    }

    public void setBillRepository(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public ItemRepository getItemRepository() {
        return itemRepository;
    }

    public void setItemRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // Service methods
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Optional<Bill> getBillById(Long id) {
        return billRepository.findById(id);
    }

    public Bill createBill(com.stockmanagement.dto.BillRequest billRequest) {
        // Validate customer
        Customer customer = customerService.getCustomerById(billRequest.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + billRequest.getCustomerId()));

        // Generate bill number
        String billNumber = "BILL-" + System.currentTimeMillis();

        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setBillNumber(billNumber);
        bill.setBillDate(LocalDateTime.now());
        bill.setStatus(BillStatus.PENDING);
        bill.setCreatedDate(LocalDateTime.now());
        bill.setUpdatedDate(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BillItem> items = new ArrayList<>();

        // Process bill items
        for (com.stockmanagement.dto.BillItemRequest itemRequest : billRequest.getItems()) {
            Item item = itemRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemRequest.getProductId()));

            if (item.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item: " + item.getName() +
                        ". Available: " + item.getQuantity() +
                        ", Requested: " + itemRequest.getQuantity());
            }

            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setItem(item);
            billItem.setQuantity(itemRequest.getQuantity());
            billItem.setUnitPrice(item.getPrice());
            billItem.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            items.add(billItem);
            totalAmount = totalAmount.add(billItem.getTotalPrice());

            // Update item stock through ItemService to trigger Observer Pattern
            // This will notify all observers (LowStockAlert, Email, AuditLog, AutoReorder, Dashboard)
            itemService.updateStock(item.getId().intValue(), itemRequest.getQuantity());
        }

        bill.setItems(items);
        bill.setTotalAmount(totalAmount);

        // Set subtotal and tax
        BigDecimal taxRate = new BigDecimal("0.10"); // 10% tax rate
        BigDecimal subtotal = totalAmount.divide(BigDecimal.ONE.add(taxRate), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal taxAmount = totalAmount.subtract(subtotal);

        bill.setSubtotalAmount(subtotal);
        bill.setTaxAmount(taxAmount);
        bill.setAmountPaid(BigDecimal.ZERO);

        // Set notes if provided
        if (billRequest.getNotes() != null && !billRequest.getNotes().trim().isEmpty()) {
            bill.setNotes(billRequest.getNotes());
        }

        return billRepository.save(bill);
    }

    public Bill saveBill(Bill bill) {
        bill.setUpdatedDate(LocalDateTime.now());
        return billRepository.save(bill);
    }

    public Bill updateBillStatus(Long id, BillStatus status) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));

        bill.setStatus(status);
        bill.setUpdatedDate(LocalDateTime.now());
        return billRepository.save(bill);
    }

    public void deleteBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
        billRepository.delete(bill);
    }

    public List<Bill> getBillsByCustomer(Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return billRepository.findByBillDateBetween(start, end);
    }

    public Bill getBillByBillNumber(String billNumber) {
        return billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found with number: " + billNumber));
    }

    public BigDecimal getTotalRevenue() {
        List<Bill> paidBills = billRepository.findByStatus(BillStatus.PAID);
        return paidBills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = getBillsByDateRange(startDate, endDate);

        BigDecimal totalRevenue = bills.stream()
                .filter(bill -> bill.getStatus() == BillStatus.PAID)
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalBills = bills.size();
        long paidBills = bills.stream().filter(bill -> bill.getStatus() == BillStatus.PAID).count();
        long pendingBills = bills.stream().filter(bill -> bill.getStatus() == BillStatus.PENDING).count();

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("totalBills", totalBills);
        report.put("paidBills", paidBills);
        report.put("pendingBills", pendingBills);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("bills", bills);

        return report;
    }
}

// BillItemRequest class moved to com.stockmanagement.dto package

// Enhanced BillResponse DTO for API responses
class BillResponse {
    private Long id;
    private String billNumber;
    private BigDecimal totalAmount;
    private LocalDateTime billDate;
    private BillStatus status;
    private CustomerInfo customer;
    private List<BillItemResponse> items;
    private LocalDateTime createdDate;

    // Default constructor
    public BillResponse() {
        this.items = new ArrayList<>();
    }

    // Constructor from Bill entity
    public BillResponse(Bill bill) {
        this();
        this.id = bill.getId();
        this.billNumber = bill.getBillNumber();
        this.totalAmount = bill.getTotalAmount();
        this.billDate = bill.getBillDate();
        this.status = bill.getStatus();
        this.createdDate = bill.getCreatedDate();

        if (bill.getCustomer() != null) {
            this.customer = new CustomerInfo(bill.getCustomer());
        }

        if (bill.getItems() != null) {
            this.items = bill.getItems().stream()
                    .map(BillItemResponse::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public CustomerInfo getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }

    public List<BillItemResponse> getItems() {
        return items;
    }

    public void setItems(List<BillItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    // Helper methods
    public void addItem(BillItemResponse item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "BillResponse{" +
                "id=" + id +
                ", billNumber='" + billNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", billDate=" + billDate +
                ", status=" + status +
                ", customer=" + customer +
                ", items=" + items +
                ", createdDate=" + createdDate +
                '}';
    }
}

// CustomerInfo DTO for BillResponse
class CustomerInfo {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // Default constructor
    public CustomerInfo() {
    }

    // Constructor from Customer entity
    public CustomerInfo(Customer customer) {
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
        this.phone = customer.getPhone();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "CustomerInfo{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

// BillItemResponse DTO for BillResponse
class BillItemResponse {
    private Long id;
    private ProductInfo product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Default constructor
    public BillItemResponse() {
    }

    // Constructor from BillItem entity
    public BillItemResponse(BillItem billItem) {
        this.id = billItem.getId();
        this.quantity = billItem.getQuantity();
        this.unitPrice = billItem.getUnitPrice();
        this.totalPrice = billItem.getTotalPrice();

        if (billItem.getItem() != null) {
            this.product = new ProductInfo(billItem.getItem());
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductInfo getProduct() {
        return product;
    }

    public void setProduct(ProductInfo product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "BillItemResponse{" +
                "id=" + id +
                ", product=" + product +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}

// ProductInfo DTO for BillItemResponse
class ProductInfo {
    private Long id;
    private String name;
    private String description;
    private String sku;

    // Default constructor
    public ProductInfo() {
    }

    // Constructor from Item entity
    public ProductInfo(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.sku = item.getSku();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public String toString() {
        return "ProductInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                //", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }
}