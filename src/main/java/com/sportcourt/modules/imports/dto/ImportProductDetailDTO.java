package com.sportcourt.modules.imports.dto;

import java.math.BigDecimal;

public class ImportProductDetailDTO {
    private String maCtnhSp;
    private String manh;
    private String maSp;
    private String tenSp;
    private int slTheoChungTu;
    private int slThucNhap;
    private BigDecimal donGia;
    private BigDecimal vat;

    public String getMaCtnhSp() { return maCtnhSp; }
    public void setMaCtnhSp(String maCtnhSp) { this.maCtnhSp = maCtnhSp; }

    public String getManh() { return manh; }
    public void setManh(String manh) { this.manh = manh; }

    public String getMaSp() { return maSp; }
    public void setMaSp(String maSp) { this.maSp = maSp; }

    public String getTenSp() { return tenSp; }
    public void setTenSp(String tenSp) { this.tenSp = tenSp; }

    public int getSlTheoChungTu() { return slTheoChungTu; }
    public void setSlTheoChungTu(int slTheoChungTu) { this.slTheoChungTu = slTheoChungTu; }

    public int getSlThucNhap() { return slThucNhap; }
    public void setSlThucNhap(int slThucNhap) { this.slThucNhap = slThucNhap; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getVat() { return vat; }
    public void setVat(BigDecimal vat) { this.vat = vat; }
}
