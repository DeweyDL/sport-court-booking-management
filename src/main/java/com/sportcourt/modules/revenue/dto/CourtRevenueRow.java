package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;

public class CourtRevenueRow {
    private String maSan;
    private String maKv;
    private String tenChiNhanh;
    private BigDecimal doanhThuThueSan;

    public String getMaSan() { return maSan; }
    public void setMaSan(String maSan) { this.maSan = maSan; }

    public String getMaKv() { return maKv; }
    public void setMaKv(String maKv) { this.maKv = maKv; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public BigDecimal getDoanhThuThueSan() { return doanhThuThueSan; }
    public void setDoanhThuThueSan(BigDecimal doanhThuThueSan) { this.doanhThuThueSan = doanhThuThueSan; }
}
