package com.sportcourt.modules.court.dto;

public class CourtSearchCriteria {
    private String branchId;
    private String keyword;
    private String areaId;
    private String status;

    public CourtSearchCriteria() {

    }

    public CourtSearchCriteria(String branchId, String keyword, String areaId, String status) {
        this.branchId = branchId;
        this.keyword = keyword;
        this.areaId = areaId;
        this.status = status;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
}
