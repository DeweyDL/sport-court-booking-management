package com.sportcourt.modules.bill.dto;

import java.math.BigDecimal;

public record ServiceItem(
        String maCTHDDV,
        String maSP,
        String maDC,
        String tenSanPham,
        int soLuong,
        BigDecimal donGia,
        String trangThai
) {}
