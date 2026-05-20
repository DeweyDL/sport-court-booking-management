package com.sportcourt.modules.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerSummary(
        String maKhachHang,
        String userId,
        String hoTen,
        String sdt,
        String diaChi,
        LocalDate ngaySinh,
        String hangKhachHang,
        String trangThai,
        BigDecimal doanhThu
) {
}

