package com.sportcourt.modules.staff.dto;
import java.time.LocalDate;
import java.math.BigDecimal;

public class StaffDetailResponse {
    private String maNv;
    private String userId;

    private String hoTen;
    private LocalDate ngaySinh;
    private String sdt;
    private String email;
    private String diaChi;
    private String maCn;
    private String diaChiChiNhanh;
    private String maLoaiNv;
    private String viTri;
    private BigDecimal mucLuong;
    private LocalDate ngayVaoLam;
    private String cccd;
    private boolean quanLy;

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

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
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

    public String getMaLoaiNv() {
        return maLoaiNv;
    }

    public void setMaLoaiNv(String maLoaiNv) {
        this.maLoaiNv = maLoaiNv;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public BigDecimal getMucLuong() {
        return mucLuong;
    }

    public void setMucLuong(BigDecimal mucLuong) {
        this.mucLuong = mucLuong;
    }
}