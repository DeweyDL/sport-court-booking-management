package com.sportcourt.modules.customer_history.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ từ bảng CHI_TIET_HOA_DON_THUE_SAN.
 */
public class CourtBookingDetail {

    private String bookingDetailId;     // MACT_THUE_SAN
    private String invoiceId;           // MAHD
    private String courtId;             // MASAN
    private String priceSlotId;         // MABG
    private LocalDateTime bookingDate;  // NGAYTHUE
    private BigDecimal unitPrice;       // DON_GIA_THUE
    private String status;              // TRANGTHAI
    private LocalDateTime createdAt;
    private boolean deleted;            // IS_DELETED

    // --- joined fields (không lưu DB, dùng khi JOIN) ---
    private int startHour;             // GIOBATDAU  (từ BANG_GIA)
    private int endHour;               // GIOKETTHUC (từ BANG_GIA)
    private String courtAreaId;        // MAKV        (từ SAN_CON → KHU_VUC)
    private String sportTypeName;      // TEN          (từ LOAI_THE_THAO)
    private String branchName;         // TEN_CHI_NHANH (từ CHI_NHANH)
    private String branchAddress;      // DIACHI        (từ CHI_NHANH)

    public CourtBookingDetail() {}

    public String getBookingDetailId()              { return bookingDetailId; }
    public void setBookingDetailId(String v)        { this.bookingDetailId = v; }

    public String getInvoiceId()                    { return invoiceId; }
    public void setInvoiceId(String v)              { this.invoiceId = v; }

    public String getCourtId()                      { return courtId; }
    public void setCourtId(String v)                { this.courtId = v; }

    public String getPriceSlotId()                  { return priceSlotId; }
    public void setPriceSlotId(String v)            { this.priceSlotId = v; }

    public LocalDateTime getBookingDate()           { return bookingDate; }
    public void setBookingDate(LocalDateTime v)     { this.bookingDate = v; }

    public BigDecimal getUnitPrice()                { return unitPrice; }
    public void setUnitPrice(BigDecimal v)          { this.unitPrice = v; }

    public String getStatus()                       { return status; }
    public void setStatus(String v)                 { this.status = v; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime v)       { this.createdAt = v; }

    public boolean isDeleted()                      { return deleted; }
    public void setDeleted(boolean v)               { this.deleted = v; }

    public int getStartHour()                       { return startHour; }
    public void setStartHour(int v)                 { this.startHour = v; }

    public int getEndHour()                         { return endHour; }
    public void setEndHour(int v)                   { this.endHour = v; }

    public String getCourtAreaId()                  { return courtAreaId; }
    public void setCourtAreaId(String v)            { this.courtAreaId = v; }

    public String getSportTypeName()                { return sportTypeName; }
    public void setSportTypeName(String v)          { this.sportTypeName = v; }

    public String getBranchName()                   { return branchName; }
    public void setBranchName(String v)             { this.branchName = v; }

    public String getBranchAddress()                { return branchAddress; }
    public void setBranchAddress(String v)          { this.branchAddress = v; }

    /** Tiện ích: trả về chuỗi giờ dạng "HH:00 - HH:00" */
    public String getTimeSlotLabel() {
        return String.format("%02d:00 - %02d:00", startHour, endHour);
    }
}