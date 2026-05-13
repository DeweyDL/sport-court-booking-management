package com.sportcourt.modules.branch.dto;

public record BranchCreateRequest(
        String maCn,
        String tenChiNhanh,
        String diaChi,
        String hotline
) {
}

