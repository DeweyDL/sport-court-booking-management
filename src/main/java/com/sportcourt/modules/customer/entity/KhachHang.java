package com.sportcourt.modules.customer.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KhachHang(
        String maKh,
        String userId,
        String maHang,
        String trangThai,
        BigDecimal doanhThu,
        LocalDateTime createdAt,
        boolean isDeleted
) {
}
