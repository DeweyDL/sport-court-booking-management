package com.sportcourt.modules.customer.view;

import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerSummary;

import java.math.BigDecimal;
import java.time.LocalDate;

final class CustomerVm {
    private final String maKhachHang;
    private final String userId;
    private final String accountId;
    private String hoTen;
    private String sdt;
    private String diaChi;
    private LocalDate ngaySinh;
    private String emailHeThong;
    private String username;
    private String trangThai;
    private String maHang;
    private BigDecimal doanhThu;

    private CustomerVm(
            String maKhachHang,
            String userId,
            String accountId,
            String hoTen,
            String sdt,
            String diaChi,
            LocalDate ngaySinh,
            String emailHeThong,
            String username,
            String trangThai,
            String maHang,
            BigDecimal doanhThu
    ) {
        this.maKhachHang = maKhachHang;
        this.userId = userId;
        this.accountId = accountId;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.ngaySinh = ngaySinh;
        this.emailHeThong = emailHeThong;
        this.username = username;
        this.trangThai = trangThai;
        this.maHang = maHang;
        this.doanhThu = doanhThu;
    }

    static CustomerVm fromSummary(CustomerSummary summary) {
        return new CustomerVm(
                summary.maKhachHang(),
                summary.userId(),
                null,
                summary.hoTen(),
                summary.sdt(),
                summary.diaChi(),
                summary.ngaySinh(),
                "",
                "",
                summary.trangThai(),
                summary.hangKhachHang(),
                summary.doanhThu()
        );
    }

    void applyProfile(CustomerProfile profile) {
        this.hoTen = profile.hoTen();
        this.sdt = profile.sdt();
        this.diaChi = profile.diaChi();
        this.ngaySinh = profile.ngaySinh();
        this.emailHeThong = profile.emailHeThong();
        this.username = profile.username();
        this.trangThai = profile.trangThai();
        this.maHang = profile.maHang();
        this.doanhThu = profile.doanhThu();
    }

    String getMaKhachHang() {
        return maKhachHang;
    }

    String getUserId() {
        return userId;
    }

    String getAccountId() {
        return accountId;
    }

    String getHoTen() {
        return hoTen;
    }

    String getSdt() {
        return sdt;
    }

    String getEmailHeThong() {
        return emailHeThong;
    }

    String getDiaChi() {
        return diaChi;
    }

    LocalDate getNgaySinh() {
        return ngaySinh;
    }

    String getUsername() {
        return username;
    }

    String getTrangThai() {
        return trangThai;
    }

    String getMaHang() {
        return maHang;
    }

    BigDecimal getDoanhThu() {
        return doanhThu;
    }
}

