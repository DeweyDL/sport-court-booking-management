package com.sportcourt.modules.managecustomer.dto;

public record UpdateCustomerRequest(
        String hoTen,
        String sdt,
        String trangThai,
        String emailHeThong,
        String username,
        String diaChi
) {
}
