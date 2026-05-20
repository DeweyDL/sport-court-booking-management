package com.sportcourt.modules.customer_history.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HistoryDetail {

    private String invoiceId;
    private String customerId;
    private String employeeId;
    private BigDecimal deposit;
    private BigDecimal discount;
    private BigDecimal totalValue;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private boolean deleted;

    public HistoryDetail() {
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String v) {
        this.invoiceId = v;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String v) {
        this.customerId = v;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String v) {
        this.employeeId = v;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal v) {
        this.deposit = v;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal v) {
        this.discount = v;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal v) {
        this.totalValue = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal v) {
        this.totalAmount = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean v) {
        this.deleted = v;
    }
}