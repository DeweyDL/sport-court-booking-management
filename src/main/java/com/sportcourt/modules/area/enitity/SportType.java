package com.sportcourt.modules.area.enitity;

public record SportType(
        String maTt,
        String ten
) {
    @Override
    public String toString() {
        return ten == null || ten.isBlank() ? maTt : ten;
    }
}
