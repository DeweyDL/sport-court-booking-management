package com.sportcourt.modules.cost.dto;

import java.math.BigDecimal;

public class CostUpdateRequest {
    private String maBg;
    private String maKv;
    private int gioBatDau;
    private int gioKetThuc;
    private BigDecimal gia;

    public CostUpdateRequest() {
    }

    public String getMaBg() {
        return maBg;
    }

    public void setMaBg(String maBg) {
        this.maBg = maBg;
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
