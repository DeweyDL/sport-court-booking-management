package com.sportcourt.court.entity;

import java.time.LocalDateTime;

public class Court {
    private String CourtID;
    private String AreaID;
    private String Status;
    private LocalDateTime createdAt;
    private boolean deleted;

    public String getCourtID() {
        return CourtID;
    }

    public void setCourtID(String CourtID) {
        this.CourtID = CourtID;
    }

    public String getAreaID() {
        return AreaID;
    }

    public void setAreaID(String AreaID) {
        this.AreaID = AreaID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
