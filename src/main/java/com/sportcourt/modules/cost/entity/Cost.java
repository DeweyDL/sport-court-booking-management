package com.sportcourt.modules.cost.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cost {
    private String maBg;
    private String maKv;
    private int gioBatDau;
    private int gioKetThuc;
    private BigDecimal gia;
    private boolean deleted;
    private LocalDateTime createdAt;

    public Cost() {}

    public Cost(String maBg, String maKv, int gioBatDau, int gioKetThuc, BigDecimal gia, boolean deleted, LocalDateTime createdAt) {
        this.maBg = maBg;
        this.maKv = maKv;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.gia = gia;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public String getMaBg() { return maBg; }
    public void setMaBg(String maBg) { this.maBg = maBg; }
    public String getMaKv() { return maKv; }
    public void setMaKv(String maKv) { this.maKv = maKv; }
    public int getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(int gioBatDau) { this.gioBatDau = gioBatDau; }
    public int getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(int gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public BigDecimal getGia() { return gia; }
    public void setGia(BigDecimal gia) { this.gia = gia; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
