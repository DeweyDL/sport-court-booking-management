package com.sportcourt.modules.court.entity;

import java.time.LocalDateTime;

public class Court {
    private String courtId;
    private String areaId;
    private String status;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public Court() {
    }

    public Court(String courtId, String areaId, String status, LocalDateTime createdAt, boolean isDeleted) {
        this.courtId = courtId;
        this.areaId = areaId;
        this.status = status;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
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

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
