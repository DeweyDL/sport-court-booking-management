package com.sportcourt.modules.area.enitity;

public record ChiNhanh(
        String maCn,
        String tenChiNhanh
) {
    @Override
    public String toString() {
        return tenChiNhanh == null || tenChiNhanh.isBlank() ? maCn : tenChiNhanh;
    }
}
