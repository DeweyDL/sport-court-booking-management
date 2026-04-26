package com.sportcourt.modules.staff.dto;

import java.time.LocalDate;

public class StaffResponse {
    private String maNv;
    private String hoTen;
    private String sdt;
    private String email;
    private String viTri;
    private String maCn;
    private String diaChiChiNhanh;
    private LocalDate ngayVaoLam;
    private String cccd;
    private boolean quanLy;

    public String getMaNv() {
        return maNv;
    }

    public void setMaNv(String maNv) {
        this.maNv = maNv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public String getMaCn() {
        return maCn;
    }

    public void setMaCn(String maCn) {
        this.maCn = maCn;
    }

    public String getDiaChiChiNhanh() {
        return diaChiChiNhanh;
    }

    public void setDiaChiChiNhanh(String diaChiChiNhanh) {
        this.diaChiChiNhanh = diaChiChiNhanh;
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
}
