package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueCreateRequest {
    private final String maDt;
    private final String maCn;
    private final String loai;          // NGAY, TUAN, THANG, NAM
    private final String noiDung;
    private final LocalDate ngay;       // ngày tạo báo cáo
    private final LocalDate ngayBatDau;
    private final LocalDate ngayKetThuc;
    private final BigDecimal dtThueSan;
    private final BigDecimal dtDichVu;
    private final BigDecimal tongDoanhThu;

    public RevenueCreateRequest(String maDt, String maCn, String loai, String noiDung,
                                LocalDate ngay, LocalDate ngayBatDau, LocalDate ngayKetThuc,
                                BigDecimal dtThueSan, BigDecimal dtDichVu, BigDecimal tongDoanhThu) {
        this.maDt          = maDt;
        this.maCn          = maCn;
        this.loai          = loai;
        this.noiDung       = noiDung;
        this.ngay          = ngay;
        this.ngayBatDau    = ngayBatDau;
        this.ngayKetThuc   = ngayKetThuc;
        this.dtThueSan     = dtThueSan;
        this.dtDichVu      = dtDichVu;
        this.tongDoanhThu  = tongDoanhThu;
    }

    public String getMaDt()              { return maDt; }
    public String getMaCn()              { return maCn; }
    public String getLoai()              { return loai; }
    public String getNoiDung()           { return noiDung; }
    public LocalDate getNgay()           { return ngay; }
    public LocalDate getNgayBatDau()     { return ngayBatDau; }
    public LocalDate getNgayKetThuc()    { return ngayKetThuc; }
    public BigDecimal getDtThueSan()     { return dtThueSan; }
    public BigDecimal getDtDichVu()      { return dtDichVu; }
    public BigDecimal getTongDoanhThu()  { return tongDoanhThu; }
}
