package com.sportcourt.modules.bill.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChiTietHoaDonThueSan(
        String maCtThueSan,
        String maHd,
        String maSan,
        String maBg,
        LocalDateTime ngayThue,
        BigDecimal donGiaThue,
        String trangThai,
        LocalDateTime createdAt,
        boolean isDeleted
) {
}
