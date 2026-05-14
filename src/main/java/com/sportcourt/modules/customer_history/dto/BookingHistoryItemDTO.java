package com.sportcourt.modules.customer_history.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng cho danh sách lịch sử đặt sân (list view).
 * Mỗi row là 1 hóa đơn kèm thông tin tóm tắt.
 */
public class BookingHistoryItemDTO {

    private String invoiceId;           // MAHD
    private String sportTypeName;       // Loại thể thao (từ LOAI_THE_THAO.TEN)
    private String branchName;          // Tên chi nhánh (từ CHI_NHANH.TEN_CHI_NHANH)
    private String branchAddress;       // Địa chỉ chi nhánh
    private LocalDateTime bookingDate;  // Ngày thuê sân đầu tiên trong hóa đơn
    private BigDecimal totalAmount;     // TONGTIEN
    private String status;              // TRANGTHAI của HOA_DON
    private int courtCount;             // Số lượng sân trong hóa đơn
    private LocalDateTime createdAt;    // CREATED_AT của HOA_DON

    public BookingHistoryItemDTO() {}

    public String getInvoiceId()                    { return invoiceId; }
    public void setInvoiceId(String v)              { this.invoiceId = v; }

    public String getSportTypeName()                { return sportTypeName; }
    public void setSportTypeName(String v)          { this.sportTypeName = v; }

    public String getBranchName()                   { return branchName; }
    public void setBranchName(String v)             { this.branchName = v; }

    public String getBranchAddress()                { return branchAddress; }
    public void setBranchAddress(String v)          { this.branchAddress = v; }

    public LocalDateTime getBookingDate()           { return bookingDate; }
    public void setBookingDate(LocalDateTime v)     { this.bookingDate = v; }

    public BigDecimal getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(BigDecimal v)        { this.totalAmount = v; }

    public String getStatus()                       { return status; }
    public void setStatus(String v)                 { this.status = v; }

    public int getCourtCount()                      { return courtCount; }
    public void setCourtCount(int v)                { this.courtCount = v; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime v)       { this.createdAt = v; }

    /** Định dạng tổng tiền hiển thị, ví dụ: "400.000 VNĐ" */
    public String getFormattedTotalAmount() {
        if (totalAmount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", totalAmount).replace(",", ".");
    }

    /** Định dạng ngày đặt sân hiển thị: dd/MM/yyyy */
    public String getFormattedBookingDate() {
        if (bookingDate == null) return "--";
        return String.format("%02d/%02d/%d",
                bookingDate.getDayOfMonth(),
                bookingDate.getMonthValue(),
                bookingDate.getYear());
    }
}