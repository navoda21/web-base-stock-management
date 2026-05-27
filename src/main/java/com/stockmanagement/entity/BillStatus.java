package com.stockmanagement.entity;

/**
 * Enum representing the possible states of a bill.
 * PENDING: The bill has been created but not yet paid
 * PARTIAL: The bill has been partially paid
 * PAID: The bill has been fully paid
 * CANCELLED: The bill has been cancelled
 */
public enum BillStatus {
    PENDING, PARTIAL, PAID, CANCELLED
}
