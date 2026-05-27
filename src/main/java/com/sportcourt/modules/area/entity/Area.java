package com.sportcourt.modules.area.entity;

import java.time.LocalDateTime;

public record Area(
        String maKv,
        String maCn,
        String maTt,
        String tenTheThao,
        int soLuongSan,
        LocalDateTime createdAt,
        boolean isDeleted // Thêm trường isDeleted
) {
    public String getStatus() {
        return isDeleted ? "Không hoạt động" : "Hoạt động";
    }

    public record ChiNhanhOption(
            String maCn,
            String tenChiNhanh
    ) {
        @Override
        public String toString() {
            return tenChiNhanh == null || tenChiNhanh.isBlank() ? maCn : tenChiNhanh;
        }
    }

    public record SportTypeOption(
            String maTt,
            String tenTheThao
    ) {
        @Override
        public String toString() {
            return tenTheThao == null || tenTheThao.isBlank() ? maTt : tenTheThao;
        }
    }
}
