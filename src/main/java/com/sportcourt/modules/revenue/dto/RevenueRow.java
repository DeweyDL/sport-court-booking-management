package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueRow {
    private String maDt;
    private String maCn;
    private String tenChiNhanh;
    private String loai;
    private String noiDung;
    private LocalDate ngay;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private BigDecimal dtThueSan;
    private BigDecimal dtDichVu;
    private BigDecimal tongDoanhThu;

    public String getMaDt() { return maDt; }
    public void setMaDt(String maDt) { this.maDt = maDt; }

    public String getMaCn() { return maCn; }
    public void setMaCn(String maCn) { this.maCn = maCn; }

    public String getTenChiNhanh() { return tenChiNhanh; }
    public void setTenChiNhanh(String tenChiNhanh) { this.tenChiNhanh = tenChiNhanh; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public LocalDate getNgay() { return ngay; }
    public void setNgay(LocalDate ngay) { this.ngay = ngay; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public BigDecimal getDtThueSan() { return dtThueSan; }
    public void setDtThueSan(BigDecimal dtThueSan) { this.dtThueSan = dtThueSan; }

    public BigDecimal getDtDichVu() { return dtDichVu; }
    public void setDtDichVu(BigDecimal dtDichVu) { this.dtDichVu = dtDichVu; }

    public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }
}
