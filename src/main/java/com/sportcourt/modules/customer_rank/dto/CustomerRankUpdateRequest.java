package com.sportcourt.modules.customer_rank.dto;

import java.math.BigDecimal;

public class CustomerRankUpdateRequest {
    private String maHang;
    private String tenHang;
    private BigDecimal chietKhau;
    private BigDecimal mucTien;

    public CustomerRankUpdateRequest() {
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
}
