package com.sportcourt.modules.area.enitity;

import java.time.LocalDateTime;

// Entity cho bang KHU_VUC. Ten the thao duoc join tu LOAI_THE_THAO de UI hien thi than thien hon.
public record KhuVuc(
        String maKv,
        String maCn,
        String maTt,
        String tenTheThao,
        int soLuongSan,
        LocalDateTime createdAt
) {
}
