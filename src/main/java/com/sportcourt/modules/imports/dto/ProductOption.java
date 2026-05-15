package com.sportcourt.modules.imports.dto;

public record ProductOption(String maSp, String tenSp) {
    @Override
    public String toString() {
        return tenSp;
    }
}
