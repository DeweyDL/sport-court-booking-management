package com.sportcourt.modules.imports.dto;

public record EquipmentOption(String maDc, String tenDc) {
    @Override
    public String toString() {
        return tenDc;
    }
}
