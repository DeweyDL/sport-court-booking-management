package com.sportcourt.modules.court.dto;

public class CourtSearchCriteria {
    private String branchId;
    private String keyword;
    private String areaId;
    private String status;
    private String sortBy;
    private String sortDirection;

    public CourtSearchCriteria() {
    }

    public CourtSearchCriteria(String branchId,
                               String keyword,
                               String areaId,
                               String status,
                               String sortBy,
                               String sortDirection) {
        this.branchId = branchId;
        this.keyword = keyword;
        this.areaId = areaId;
        this.status = status;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public String getBranchId() {

        return branchId;
    }

    public void setBranchId(String branchId) {

        this.branchId = branchId;
    }

    public String getKeyWord() {

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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
