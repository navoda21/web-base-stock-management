package com.stockmanagement.service;

import com.stockmanagement.entity.Item;
import com.stockmanagement.entity.SupplierRequest;
import com.stockmanagement.repository.SupplierRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierRequestService {

    @Autowired
    private SupplierRequestRepository supplierRequestRepository;

    @Autowired
    private ItemService itemService;

    /**
     * Create a new supplier request for low stock item
     */
    public SupplierRequest createRequest(Long itemId, int requestedQuantity, String requestedBy, String notes) {
        Optional<Item> itemOpt = itemService.getItemById(itemId);

        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found with ID: " + itemId);
        }

        Item item = itemOpt.get();
        SupplierRequest request = new SupplierRequest(item, requestedQuantity, item.getQuantity(), requestedBy);
        request.setNotes(notes);

        return supplierRequestRepository.save(request);
    }

    /**
     * Get all supplier requests
     */
    public List<SupplierRequest> getAllRequests() {
        return supplierRequestRepository.findAll();
    }

    /**
     * Get all pending requests
     */
    public List<SupplierRequest> getPendingRequests() {
        return supplierRequestRepository.findByStatusOrderByRequestedAtDesc("PENDING");
    }

    /**
     * Get requests by status
     */
    public List<SupplierRequest> getRequestsByStatus(String status) {
        return supplierRequestRepository.findByStatus(status);
    }

    /**
     * Get requests for a specific item
     */
    public List<SupplierRequest> getRequestsByItem(Long itemId) {
        return supplierRequestRepository.findByItemId(itemId);
    }

    /**
     * Update request status
     */
    public SupplierRequest updateRequestStatus(Long requestId, String status, String processedBy) {
        Optional<SupplierRequest> requestOpt = supplierRequestRepository.findById(requestId);

        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }

        SupplierRequest request = requestOpt.get();
        request.setStatus(status);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(processedBy);

        return supplierRequestRepository.save(request);
    }

    /**
     * Get request by ID
     */
    public Optional<SupplierRequest> getRequestById(Long id) {
        return supplierRequestRepository.findById(id);
    }
}
