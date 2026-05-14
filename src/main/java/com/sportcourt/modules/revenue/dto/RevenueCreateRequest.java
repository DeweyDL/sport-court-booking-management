package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueCreateRequest {
    private String maDt;
    private String maCn;
    private String noiDung;
    private LocalDate ngay;
    private BigDecimal tongDoanhThu;

    public RevenueCreateRequest(String maDt, String maCn, String noiDung,
                                LocalDate ngay, BigDecimal tongDoanhThu) {
        this.maDt         = maDt;
        this.maCn         = maCn;
        this.noiDung       = noiDung;
        this.ngay          = ngay;
        this.tongDoanhThu  = tongDoanhThu;
    }

    public String getMaDt()              { return maDt; }
    public String getMaCn()              { return maCn; }
    public String getNoiDung()           { return noiDung; }
    public LocalDate getNgay()           { return ngay; }
    public BigDecimal getTongDoanhThu()  { return tongDoanhThu; }
}
