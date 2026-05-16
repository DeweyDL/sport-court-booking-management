package com.sportcourt.modules.bill.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BillDetail(
        String maHD,
        String maKH,
        String tenKhachHang,
        String sdtKhachHang,
        String maNV,
        String tenNhanVien,
        BigDecimal tienCoc,
        BigDecimal giamGia,
        BigDecimal tongGiaTri,
        String trangThai,
        BigDecimal tongTien,
        LocalDateTime createdAt,
        List<CourtRentalItem> danhSachThuesan,
        List<ServiceItem> danhSachDichVu
) {}
