package com.sportcourt.modules.staff.entity;

import java.time.LocalDate;

public class Staff {
    private String maNv;
    private String userId;
    private String maLoaiNv;
    private String maCn;
    private LocalDate ngayVaoLam;
    private String cccd;
    private boolean quanLy;
    private String trangThai;

    public String getMaNv() {
        return maNv;
    }

    public void setMaNv(String maNv) {
        this.maNv = maNv;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMaLoaiNv() {
        return maLoaiNv;
    }

    public void setMaLoaiNv(String maLoaiNv) {
        this.maLoaiNv = maLoaiNv;
    }

    public String getMaCn() {
        return maCn;
    }

    public void setMaCn(String maCn) {
        this.maCn = maCn;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public boolean isQuanLy() {
        return quanLy;
    }

    public void setQuanLy(boolean quanLy) {
        this.quanLy = quanLy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
