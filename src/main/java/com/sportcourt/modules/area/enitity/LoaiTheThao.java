package com.sportcourt.modules.area.enitity;

// Entity nhe cho combobox loai the thao trong man sua khu vuc.
public record LoaiTheThao(
        String maTt,
        String ten
) {
    @Override
    public String toString() {
        return ten == null || ten.isBlank() ? maTt : ten;
    }
}
