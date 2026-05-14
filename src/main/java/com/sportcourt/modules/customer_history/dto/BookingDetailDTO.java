package com.sportcourt.modules.customer_history.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO dùng cho màn hình chi tiết một hóa đơn đặt sân.
 * Gồm thông tin hóa đơn + danh sách chi tiết từng sân đã thuê.
 */
public class BookingDetailDTO {

    // --- Thông tin hóa đơn (HOA_DON) ---
    private String invoiceId;
    private String status;
    private BigDecimal deposit;
    private BigDecimal discount;
    private BigDecimal totalValue;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    // --- Thông tin khách hàng (join USERS qua KHACH_HANG) ---
    private String customerName;    // USERS.HOTEN
    private String customerPhone;   // USERS.SDT
    private String customerId;      // MAKH

    // --- Thông tin chi nhánh (join qua SAN_CON → KHU_VUC → CHI_NHANH) ---
    private String branchName;
    private String branchAddress;

    // --- Danh sách chi tiết từng sân (CHI_TIET_HOA_DON_THUE_SAN) ---
    private List<CourtLineItem> courtItems;

    public BookingDetailDTO() {}

    // ---- Getters / Setters ----

    public String getInvoiceId()                        { return invoiceId; }
    public void setInvoiceId(String v)                  { this.invoiceId = v; }

    public String getStatus()                           { return status; }
    public void setStatus(String v)                     { this.status = v; }

    public BigDecimal getDeposit()                      { return deposit; }
    public void setDeposit(BigDecimal v)                { this.deposit = v; }

    public BigDecimal getDiscount()                     { return discount; }
    public void setDiscount(BigDecimal v)               { this.discount = v; }

    public BigDecimal getTotalValue()                   { return totalValue; }
    public void setTotalValue(BigDecimal v)             { this.totalValue = v; }

    public BigDecimal getTotalAmount()                  { return totalAmount; }
    public void setTotalAmount(BigDecimal v)            { this.totalAmount = v; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime v)           { this.createdAt = v; }

    public String getCustomerName()                     { return customerName; }
    public void setCustomerName(String v)               { this.customerName = v; }

    public String getCustomerPhone()                    { return customerPhone; }
    public void setCustomerPhone(String v)              { this.customerPhone = v; }

    public String getCustomerId()                       { return customerId; }
    public void setCustomerId(String v)                 { this.customerId = v; }

    public String getBranchName()                       { return branchName; }
    public void setBranchName(String v)                 { this.branchName = v; }

    public String getBranchAddress()                    { return branchAddress; }
    public void setBranchAddress(String v)              { this.branchAddress = v; }

    public List<CourtLineItem> getCourtItems()          { return courtItems; }
    public void setCourtItems(List<CourtLineItem> v)    { this.courtItems = v; }

    /** Định dạng tổng tiền hiển thị */
    public String getFormattedTotalAmount() {
        if (totalAmount == null) return "0đ";
        return String.format("%,.0fđ", totalAmount).replace(",", ".");
    }

    public String getFormattedTotalValue() {
        if (totalValue == null) return "0đ";
        return String.format("%,.0fđ", totalValue).replace(",", ".");
    }

    // ====================================================================
    //  Inner class: từng dòng sân trong chi tiết hóa đơn
    // ====================================================================
    public static class CourtLineItem {

        private String bookingDetailId;  // MACT_THUE_SAN
        private String courtId;          // MASAN
        private String timeSlot;         // "HH:00 - HH:00"
        private LocalDateTime courtDate; // NGAYTHUE
        private BigDecimal unitPrice;    // DON_GIA_THUE
        private String status;           // TRANGTHAI chi tiết
        private String sportTypeName;    // Loại thể thao

        public CourtLineItem() {}

        public String getBookingDetailId()              { return bookingDetailId; }
        public void setBookingDetailId(String v)        { this.bookingDetailId = v; }

        public String getCourtId()                      { return courtId; }
        public void setCourtId(String v)                { this.courtId = v; }

        public String getTimeSlot()                     { return timeSlot; }
        public void setTimeSlot(String v)               { this.timeSlot = v; }

        public LocalDateTime getCourtDate()             { return courtDate; }
        public void setCourtDate(LocalDateTime v)       { this.courtDate = v; }

        public BigDecimal getUnitPrice()                { return unitPrice; }
        public void setUnitPrice(BigDecimal v)          { this.unitPrice = v; }

        public String getStatus()                       { return status; }
        public void setStatus(String v)                 { this.status = v; }

        public String getSportTypeName()                { return sportTypeName; }
        public void setSportTypeName(String v)          { this.sportTypeName = v; }

        /** Định dạng ngày thuê: dd/MM/yyyy */
        public String getFormattedCourtDate() {
            if (courtDate == null) return "--";
            return String.format("%02d/%02d/%d",
                    courtDate.getDayOfMonth(),
                    courtDate.getMonthValue(),
                    courtDate.getYear());
        }


        public String getFormattedUnitPrice() {
            if (unitPrice == null) return "0đ/giờ";
            return String.format("%,.0fđ/giờ", unitPrice).replace(",", ".");
        }
    }
}