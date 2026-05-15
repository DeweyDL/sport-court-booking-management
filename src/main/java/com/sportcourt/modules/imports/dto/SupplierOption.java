package com.sportcourt.modules.imports.dto;

public record SupplierOption(String mancc, String tenNcc) {
    @Override
    public String toString() {
        return tenNcc;
    }
}
