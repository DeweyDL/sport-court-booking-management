package com.sportcourt.modules.staff.dto;

public class StaffSearchCriteria {
    private String keyword;
    private String maCn;
    private String maLoaiNv;
    private Boolean quanLy;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getMaCn() {
        return maCn;
    }

    public void setMaCn(String maCn) {
        this.maCn = maCn;
    }

    public String getMaLoaiNv() {
        return maLoaiNv;
    }

    public void setMaLoaiNv(String maLoaiNv) {
        this.maLoaiNv = maLoaiNv;
    }

    public Boolean getQuanLy() {
        return quanLy;
    }

    public void setQuanLy(Boolean quanLy) {
        this.quanLy = quanLy;
    }
}