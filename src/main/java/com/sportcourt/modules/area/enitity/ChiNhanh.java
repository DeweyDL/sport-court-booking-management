package com.sportcourt.modules.area.enitity;

// Entity nhe cho combobox chi nhanh trong man them khu vuc.
public record ChiNhanh(
        String maCn,
        String tenChiNhanh
) {
    @Override
    public String toString() {
        return tenChiNhanh == null || tenChiNhanh.isBlank() ? maCn : tenChiNhanh;
    }
}
