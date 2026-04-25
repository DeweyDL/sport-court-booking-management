package com.sportcourt.modules.area.enitity;

import java.util.List;

// DTO tong hop cac thay doi o man sua de service/dao co the luu trong cung mot luong.
public record KhuVucUpdateRequest(
        String maKv,
        String maTt,
        int soLuongSan,
        List<SanConDraft> newSanCons,
        List<String> deletedSanConIds
) {
}
