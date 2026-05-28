package com.sportcourt.modules.bill.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChiTietHoaDonDichVuDaDung(
        String maCtDichVu,
        String maHd,
        String maSp,
        String maDc,
        int soLuong,
        BigDecimal donGia,
        String trangThai,
        LocalDateTime createdAt,
        boolean isDeleted
) {
}
