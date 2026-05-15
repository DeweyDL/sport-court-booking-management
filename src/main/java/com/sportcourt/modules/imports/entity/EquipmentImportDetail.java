package com.sportcourt.modules.imports.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EquipmentImportDetail {
    private String maCtnhDc;
    private String manh;
    private String maDc;
    private int slTheoChungTu;
    private int slThucNhap;
    private BigDecimal cktm;
    private BigDecimal donGia;
    private LocalDateTime createdAt;
    private boolean deleted;

    public String getMaCtnhDc() { return maCtnhDc; }
    public void setMaCtnhDc(String maCtnhDc) { this.maCtnhDc = maCtnhDc; }

    public String getManh() { return manh; }
    public void setManh(String manh) { this.manh = manh; }

    public String getMaDc() { return maDc; }
    public void setMaDc(String maDc) { this.maDc = maDc; }

    public int getSlTheoChungTu() { return slTheoChungTu; }
    public void setSlTheoChungTu(int slTheoChungTu) { this.slTheoChungTu = slTheoChungTu; }

    public int getSlThucNhap() { return slThucNhap; }
    public void setSlThucNhap(int slThucNhap) { this.slThucNhap = slThucNhap; }

    public BigDecimal getCktm() { return cktm; }
    public void setCktm(BigDecimal cktm) { this.cktm = cktm; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
