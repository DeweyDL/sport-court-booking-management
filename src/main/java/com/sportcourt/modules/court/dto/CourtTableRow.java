package com.sportcourt.modules.court.dto;

import java.time.LocalDateTime;

public class CourtTableRow {
    private String courtId;
    private String areaId;
    private String sportTypeName;
    private String branchId;
    private String branchName;
    private String status;
    private LocalDateTime createdAt;

    public CourtTableRow() {
    }

    public CourtTableRow(String courtId,
                         String areaId,
                         String sportName,
                         String branchId,
                         String branchName,
                         String status,
                         LocalDateTime createdAt) {
        this.courtId = courtId;
        this.areaId = areaId;
        this.sportTypeName = sportName;
        this.branchName = branchName;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getSportTypeName() {
        return sportTypeName;
    }

    public void setSportTypeName(String sportTypeName) {
        this.sportTypeName = sportTypeName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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
}