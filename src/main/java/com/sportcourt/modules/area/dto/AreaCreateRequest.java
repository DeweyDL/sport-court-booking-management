package com.sportcourt.modules.area.dto;

public record AreaCreateRequest(
        String maKv,
        String maCn,
        String maTt,
        int soLuongSan
        // TODO san con: them danh sach san con khi module san con hoan thien.
) {
}
