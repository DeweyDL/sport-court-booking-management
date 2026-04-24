package com.sportcourt.court.entity;

import java.time.LocalDateTime;

public class Court {
    public static final String STATUS_ACTIVE = "ĐANG HOẠT ĐỘNG";
    public static final String STATUS_MAINTENANCE = "BẢO TRÌ";

    private String courtId;
    private String areaId;
    private String status;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public Court() {
    }

    public Court(String courtId, String areaId, String status) {
        this.courtId = courtId;
        this.areaId = areaId;
        this.status = status;
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}