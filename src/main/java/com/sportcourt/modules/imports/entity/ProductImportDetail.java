package com.sportcourt.modules.imports.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductImportDetail {
    private String maCtnhSp;
    private String manh;
    private String maSp;
    private int slTheoChungTu;
    private int slThucNhap;
    private BigDecimal donGia;
    private BigDecimal vat;
    private LocalDateTime createdAt;
    private boolean deleted;

    public String getMaCtnhSp() { return maCtnhSp; }
    public void setMaCtnhSp(String maCtnhSp) { this.maCtnhSp = maCtnhSp; }

    public String getManh() { return manh; }
    public void setManh(String manh) { this.manh = manh; }

    public String getMaSp() { return maSp; }
    public void setMaSp(String maSp) { this.maSp = maSp; }

    public int getSlTheoChungTu() { return slTheoChungTu; }
    public void setSlTheoChungTu(int slTheoChungTu) { this.slTheoChungTu = slTheoChungTu; }

    public int getSlThucNhap() { return slThucNhap; }
    public void setSlThucNhap(int slThucNhap) { this.slThucNhap = slThucNhap; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getVat() { return vat; }
    public void setVat(BigDecimal vat) { this.vat = vat; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
