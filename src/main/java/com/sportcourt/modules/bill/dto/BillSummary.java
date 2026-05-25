package com.sportcourt.modules.bill.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillSummary(
        String maHD,
        String maKH,
        String tenKhachHang,
        String maNV,
        String tenNhanVien,
        BigDecimal tienCoc,
        BigDecimal giamGia,
        BigDecimal tongGiaTri,
        String trangThai,
        BigDecimal tongTien,
        LocalDateTime createdAt,
        String maCN,
        String tenChiNhanh
) {}
