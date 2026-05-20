package com.sportcourt.modules.customer_history.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CourtBookingDetail {

    private String bookingDetailId;
    private String invoiceId;
    private String courtId;
    private String priceSlotId;
    private LocalDateTime bookingDate;
    private BigDecimal unitPrice;
    private String status;
    private LocalDateTime createdAt;
    private boolean deleted;

    private int startHour;
    private int endHour;
    private String courtAreaId;
    private String sportTypeName;
    private String branchName;
    private String branchAddress;

    public CourtBookingDetail() {
    }

    public String getBookingDetailId() {
        return bookingDetailId;
    }

    public void setBookingDetailId(String v) {
        this.bookingDetailId = v;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String v) {
        this.invoiceId = v;
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String v) {
        this.courtId = v;
    }

    public String getPriceSlotId() {
        return priceSlotId;
    }

    public void setPriceSlotId(String v) {
        this.priceSlotId = v;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime v) {
        this.bookingDate = v;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal v) {
        this.unitPrice = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
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

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int v) {
        this.startHour = v;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int v) {
        this.endHour = v;
    }

    public String getCourtAreaId() {
        return courtAreaId;
    }

    public void setCourtAreaId(String v) {
        this.courtAreaId = v;
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

    public String getTimeSlotLabel() {
        return String.format("%02d:00 - %02d:00", startHour, endHour);
    }
}