package com.sportcourt.modules.bill.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourtRentalItem(
        String maCTHDTS,
        String maSan,
        String maBG,
        LocalDateTime ngayThue,
        int gioBatDau,
        int gioKetThuc,
        BigDecimal donGiaThue,
        String trangThai
) {}
