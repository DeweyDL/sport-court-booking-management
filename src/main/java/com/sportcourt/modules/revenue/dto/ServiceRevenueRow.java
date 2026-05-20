package com.sportcourt.modules.revenue.dto;

import java.math.BigDecimal;

public class ServiceRevenueRow {
    private String maItem;   // MASP hoặc MADC
    private String tenItem;  // TENSP hoặc TENDC
    private String loai;     // "Sản phẩm" hoặc "Dụng cụ"
    private BigDecimal doanhThu;

    public String getMaItem()  { return maItem; }
    public void setMaItem(String maItem) { this.maItem = maItem; }

    public String getTenItem() { return tenItem; }
    public void setTenItem(String tenItem) { this.tenItem = tenItem; }

    public String getLoai()    { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public BigDecimal getDoanhThu() { return doanhThu; }
    public void setDoanhThu(BigDecimal doanhThu) { this.doanhThu = doanhThu; }
}
