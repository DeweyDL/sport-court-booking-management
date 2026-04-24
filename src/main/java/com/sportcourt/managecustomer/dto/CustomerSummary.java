package com.sportcourt.managecustomer.dto;

import java.math.BigDecimal;

public record CustomerSummary(
        String maKhachHang,
        String userId,
        String hoTen,
        String sdt,
        String trangThai,
        BigDecimal doanhThu
) {
}
