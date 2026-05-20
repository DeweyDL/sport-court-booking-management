package com.sportcourt.modules.imports.dto;

public record EmployeeOption(String manv, String tenNv) {
    @Override
    public String toString() {
        return tenNv;
    }
}
