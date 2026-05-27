package com.stockmanagement.dto;

import java.math.BigDecimal;

public class PaymentDTO {
    private BigDecimal amount;
    private String paymentMethod;
    private String reference;
    private String note;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", reference='" + reference + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}