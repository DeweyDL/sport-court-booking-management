package com.sportcourt.modules.area.dto;

public record AreaCreateRequest(
        String maKv,
        String maCn,
        String maTt,
        int soLuongSan
) {
}
