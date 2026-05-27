package com.stockmanagement.controller;

import com.stockmanagement.dto.BillRequest;
import com.stockmanagement.entity.Bill;
import com.stockmanagement.entity.BillStatus;
import com.stockmanagement.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    @Autowired
    private BillService billService;

    @GetMapping
    public List<Bill> getAllBills() {
        return billService.getAllBills();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        Optional<Bill> bill = billService.getBillById(id);
        return bill.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBill(@RequestBody BillRequest billRequest) {
        try {
            Bill bill = billService.createBill(billRequest);
            return ResponseEntity.ok(bill);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Bill> updateBillStatus(@PathVariable Long id, @RequestParam BillStatus status) {
        try {
            Bill updatedBill = billService.updateBillStatus(id, status);
            return ResponseEntity.ok(updatedBill);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        try {
            billService.deleteBill(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public List<Bill> getBillsByCustomer(@PathVariable Long customerId) {
        return billService.getBillsByCustomer(customerId);
    }
}

