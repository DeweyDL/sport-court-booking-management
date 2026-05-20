package com.sportcourt.modules.equipment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Equipment {
    private String maDc;
    private String tenDc;
    private String dvt;
    private BigDecimal gia;
    private int slTon;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public Equipment() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
