package com.sportcourt.modules.customer_rank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerRank {
    private String maHang;
    private String tenHang;
    private BigDecimal chietKhau;
    private BigDecimal mucTien;
    private LocalDateTime createdAt;
    private boolean deleted;

    public CustomerRank() {
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public BigDecimal getChietKhau() {
        return chietKhau;
    }

    public void setChietKhau(BigDecimal chietKhau) {
        this.chietKhau = chietKhau;
    }

    public BigDecimal getMucTien() {
        return mucTien;
    }

    public void setMucTien(BigDecimal mucTien) {
        this.mucTien = mucTien;
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
