package com.sportcourt.modules.managecustomer.dto;

import java.math.BigDecimal;

public record CustomerProfile(
        String maKhachHang,
        String userId,
        String accountId,
        String hoTen,
        String sdt,
        String emailHeThong,
        String username,
        String trangThai,
        String maHang,
        BigDecimal doanhThu
) {
}
