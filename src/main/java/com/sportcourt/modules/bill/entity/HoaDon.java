package com.sportcourt.modules.bill.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HoaDon(
        String maHd,
        String maKh,
        String maNv,
        BigDecimal tienCoc,
        BigDecimal giamGia,
        BigDecimal tongGiaTri,
        String trangThai,
        BigDecimal tongTien,
        LocalDateTime createdAt,
        boolean isDeleted
) {
}
