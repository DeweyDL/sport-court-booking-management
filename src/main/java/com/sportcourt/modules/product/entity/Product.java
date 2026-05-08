package com.sportcourt.modules.product.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private String maSp;
    private String tenSp;
    private String dvt;
    private BigDecimal gia;
    private Integer slTon;
    private LocalDateTime createdAt;
    private boolean deleted;

    public String getMaSp() {
        return maSp;
    }

    public void setMaSp(String maSp) {
        this.maSp = maSp;
    }

    public String getTenSp() {
        return tenSp;
    }

    public void setTenSp(String tenSp) {
        this.tenSp = tenSp;
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

    public Integer getSlTon() {
        return slTon;
    }

    public void setSlTon(Integer slTon) {
        this.slTon = slTon;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
