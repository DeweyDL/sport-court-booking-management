package com.sportcourt.modules.customer.dto;

import java.math.BigDecimal;

public record CustomerSummary(
        String maKhachHang,
        String userId,
        String hoTen,
        String sdt,
        String hangKhachHang,
        String trangThai,
        BigDecimal doanhThu
) {
}

