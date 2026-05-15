package com.sportcourt.modules.imports.dto;

import java.math.BigDecimal;

public class ImportEquipmentDetailDTO {
    private String maCtnhDc;
    private String manh;
    private String maDc;
    private String tenDc;
    private int slTheoChungTu;
    private int slThucNhap;
    private BigDecimal cktm;
    private BigDecimal donGia;

    public String getMaCtnhDc() { return maCtnhDc; }
    public void setMaCtnhDc(String maCtnhDc) { this.maCtnhDc = maCtnhDc; }

    public String getManh() { return manh; }
    public void setManh(String manh) { this.manh = manh; }

    public String getMaDc() { return maDc; }
    public void setMaDc(String maDc) { this.maDc = maDc; }

    public String getTenDc() { return tenDc; }
    public void setTenDc(String tenDc) { this.tenDc = tenDc; }

    public int getSlTheoChungTu() { return slTheoChungTu; }
    public void setSlTheoChungTu(int slTheoChungTu) { this.slTheoChungTu = slTheoChungTu; }

    public int getSlThucNhap() { return slThucNhap; }
    public void setSlThucNhap(int slThucNhap) { this.slThucNhap = slThucNhap; }

    public BigDecimal getCktm() { return cktm; }
    public void setCktm(BigDecimal cktm) { this.cktm = cktm; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}
