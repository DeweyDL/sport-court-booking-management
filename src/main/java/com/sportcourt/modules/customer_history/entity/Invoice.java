package com.sportcourt.modules.customer_history.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ từ bảng HOA_DON.
 */
public class Invoice {

    private String invoiceId;       // MAHD
    private String customerId;      // MAKH
    private String employeeId;      // MANV
    private BigDecimal deposit;     // TIEN_COC
    private BigDecimal discount;    // GIAMGIA (%)
    private BigDecimal totalValue;  // TONGGIATRI
    private String status;          // TRANGTHAI
    private BigDecimal totalAmount; // TONGTIEN
    private LocalDateTime createdAt;
    private boolean deleted;        // IS_DELETED

    public Invoice() {}

    public String getInvoiceId()               { return invoiceId; }
    public void setInvoiceId(String v)         { this.invoiceId = v; }

    public String getCustomerId()              { return customerId; }
    public void setCustomerId(String v)        { this.customerId = v; }

    public String getEmployeeId()              { return employeeId; }
    public void setEmployeeId(String v)        { this.employeeId = v; }

    public BigDecimal getDeposit()             { return deposit; }
    public void setDeposit(BigDecimal v)       { this.deposit = v; }

    public BigDecimal getDiscount()            { return discount; }
    public void setDiscount(BigDecimal v)      { this.discount = v; }

    public BigDecimal getTotalValue()          { return totalValue; }
    public void setTotalValue(BigDecimal v)    { this.totalValue = v; }

    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }

    public BigDecimal getTotalAmount()         { return totalAmount; }
    public void setTotalAmount(BigDecimal v)   { this.totalAmount = v; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }

    public boolean isDeleted()                 { return deleted; }
    public void setDeleted(boolean v)          { this.deleted = v; }
}