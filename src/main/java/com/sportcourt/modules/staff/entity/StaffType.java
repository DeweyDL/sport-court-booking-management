package com.sportcourt.modules.staff.entity;

import java.math.BigDecimal;

public class StaffType {
    private String maLoaiNv;
    private String viTri;
    private BigDecimal mucLuong;

    public String getMaLoaiNv() {
        return maLoaiNv;
    }

    public void setMaLoaiNv(String maLoaiNv) {
        this.maLoaiNv = maLoaiNv;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public BigDecimal getMucLuong() {
        return mucLuong;
    }

    public void setMucLuong(BigDecimal mucLuong) {
        this.mucLuong = mucLuong;
    }

    @Override
    public String toString() {
        return maLoaiNv;
    }
}
