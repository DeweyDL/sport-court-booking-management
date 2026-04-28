package com.sportcourt.modules.area.enitity;

import java.time.LocalDateTime;

public record Area(
        String maKv,
        String maCn,
        String maTt,
        String tenTheThao,
        int soLuongSan,
        LocalDateTime createdAt
) {
}
