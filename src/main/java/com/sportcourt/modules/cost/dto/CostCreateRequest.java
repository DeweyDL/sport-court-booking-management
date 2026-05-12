package com.sportcourt.modules.cost.dto;

import java.math.BigDecimal;

public class CostCreateRequest {
    private String maKv;
    private int gioBatDau;
    private int gioKetThuc;
    private BigDecimal gia;

    public CostCreateRequest() {
    }

    public String getMaKv() {
        return maKv;
    }

    public void setMaKv(String maKv) {
        this.maKv = maKv;
    }

    public int getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(int gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public int getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(int gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }
}
