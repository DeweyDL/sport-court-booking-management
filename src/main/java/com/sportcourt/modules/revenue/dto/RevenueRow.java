package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueRow {
    private String maDt;
    private String maCn;
    private String tenChiNhanh;
    private String noiDung;
    private LocalDate ngay;
    private BigDecimal tongDoanhThu;

    public String getMaDt() { return maDt; }
    public void setMaDt(String maDt) { this.maDt = maDt; }

    public String getMaCn() { return maCn; }
    public void setMaCn(String maCn) { this.maCn = maCn; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }
}
