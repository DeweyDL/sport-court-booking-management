package com.sportcourt.modules.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerProfile(
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
}

