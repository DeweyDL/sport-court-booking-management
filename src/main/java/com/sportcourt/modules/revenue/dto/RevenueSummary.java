package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;

public class RevenueSummary {
    private BigDecimal tongDoanhThu;
    private BigDecimal doanhThuThueSan;
    private BigDecimal doanhThuDichVu;

    public BigDecimal getTongDoanhThu()   { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal v) { this.tongDoanhThu = v; }

    public BigDecimal getDoanhThuThueSan()   { return doanhThuThueSan; }
    public void setDoanhThuThueSan(BigDecimal v) { this.doanhThuThueSan = v; }

    public BigDecimal getDoanhThuDichVu()   { return doanhThuDichVu; }
    public void setDoanhThuDichVu(BigDecimal v) { this.doanhThuDichVu = v; }
}
