package com.sportcourt.modules.area.enitity;

import java.time.LocalDateTime;

// Entity cho bang SAN_CON de man chi tiet khu vuc render danh sach san con tu du lieu that.
public record Court(
        String maSan,
        String maKv,
        String trangThai,
        LocalDateTime createdAt
) {
}
