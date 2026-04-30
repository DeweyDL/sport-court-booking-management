package com.sportcourt.modules.branch.dto;

public record BranchUpdateRequest(
        String maCn,
        String tenChiNhanh,
        String diaChi,
        String hotline
) {
}

