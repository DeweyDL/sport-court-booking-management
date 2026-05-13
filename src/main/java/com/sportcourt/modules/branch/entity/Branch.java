package com.sportcourt.modules.branch.entity;

import java.time.LocalDateTime;

public record Branch(
        String maCn,
        String tenChiNhanh,
        String diaChi,
        String hotline,
        LocalDateTime createdAt,
        boolean isDeleted
) {
    public String getStatus() {
        return isDeleted ? "Không hoạt động" : "Hoạt động";
    }
}
