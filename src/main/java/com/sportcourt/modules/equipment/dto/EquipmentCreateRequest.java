package com.sportcourt.modules.equipment.dto;

import java.math.BigDecimal;

public class EquipmentCreateRequest {
    private String maDc;
    private String tenDc;
    private String dvt;
    private BigDecimal gia;
    private int slTon;

    public EquipmentCreateRequest() {
    }

    public String getMaDc() {
        return maDc;
    }

    public void setMaDc(String maDc) {
        this.maDc = maDc;
    }

    public String getTenDc() {
        return tenDc;
    }

    public void setTenDc(String tenDc) {
        this.tenDc = tenDc;
    }

    public String getDvt() {
        return dvt;
    }

    public void setDvt(String dvt) {
        this.dvt = dvt;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public int getSlTon() {
        return slTon;
    }

    public void setSlTon(int slTon) {
        this.slTon = slTon;
    }
}
