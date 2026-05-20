package com.sportcourt.modules.customer_history.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingHistoryItemDTO {

    private String invoiceId;
    private String sportTypeName;
    private String branchName;
    private String branchAddress;
    private LocalDateTime bookingDate;
    private BigDecimal totalAmount;
    private String status;
    private int courtCount;
    private LocalDateTime createdAt;

    public BookingHistoryItemDTO() {
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String v) {
        this.invoiceId = v;
    }

    public String getSportTypeName() {
        return sportTypeName;
    }

    public void setSportTypeName(String v) {
        this.sportTypeName = v;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String v) {
        this.branchName = v;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String v) {
        this.branchAddress = v;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime v) {
        this.bookingDate = v;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal v) {
        this.totalAmount = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public int getCourtCount() {
        return courtCount;
    }

    public void setCourtCount(int v) {
        this.courtCount = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }

    public String getFormattedTotalAmount() {
        if (totalAmount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", totalAmount).replace(",", ".");
    }

    public String getFormattedBookingDate() {
        if (bookingDate == null) return "--";
        return String.format("%02d/%02d/%d",
                bookingDate.getDayOfMonth(),
                bookingDate.getMonthValue(),
                bookingDate.getYear());
    }

    public boolean matchSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String kw = keyword.trim().toLowerCase();
        return (invoiceId != null && invoiceId.toLowerCase().contains(kw))
                || (sportTypeName != null && sportTypeName.toLowerCase().contains(kw))
                || (branchName != null && branchName.toLowerCase().contains(kw))
                || (status != null && status.toLowerCase().contains(kw));
    }
}