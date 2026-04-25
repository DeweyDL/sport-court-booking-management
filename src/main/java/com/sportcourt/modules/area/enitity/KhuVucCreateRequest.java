package com.sportcourt.modules.area.enitity;

import java.util.List;

// DTO dung cho luong tao moi khu vuc va cac san con di kem.
public record KhuVucCreateRequest(
        String maKv,
        String maCn,
        String maTt,
        int soLuongSan,
        List<SanConDraft> sanCons
) {
}
