package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;

public class BranchRevenueRow {
    private String maCn;
    private String tenChiNhanh;
    private BigDecimal tongDoanhThu;
    private BigDecimal doanhThuThueSan;
    private BigDecimal doanhThuDichVu;

    public String getMaCn() { return maCn; }
    public void setMaCn(String maCn) { this.maCn = maCn; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

    public BigDecimal getDoanhThuThueSan() { return doanhThuThueSan; }
    public void setDoanhThuThueSan(BigDecimal doanhThuThueSan) { this.doanhThuThueSan = doanhThuThueSan; }

    public BigDecimal getDoanhThuDichVu() { return doanhThuDichVu; }
    public void setDoanhThuDichVu(BigDecimal doanhThuDichVu) { this.doanhThuDichVu = doanhThuDichVu; }
}
