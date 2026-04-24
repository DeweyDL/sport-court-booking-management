package com.sportcourt.court.dto;

import java.time.LocalDateTime;

public class CourtTableRow {
    private String courtId;
    private String areaId;
    private String sportTypeName;
    private String branchId;
    private String branchName;
    private String status;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public CourtTableRow() {

    }

    public CourtTableRow(
            String courtId,
            String areaId,
            String sportTypeName,
            String branchId,
            String branchName,
            String status,
            LocalDateTime createdAt,
            boolean isDeleted
    ) {
        this.courtId = courtId;
        this.areaId = areaId;
        this.sportTypeName = sportTypeName;
        this.branchId = branchId;
        this.branchName = branchName;
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

    public String getSportTypeName() {
        return sportTypeName;
    }

    public void setSportTypeName(String sportTypeName) {
        this.sportTypeName = sportTypeName;
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
