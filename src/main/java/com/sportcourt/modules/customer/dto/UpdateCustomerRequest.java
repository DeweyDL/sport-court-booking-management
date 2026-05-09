package com.sportcourt.modules.customer.dto;

import java.time.LocalDate;

public record UpdateCustomerRequest(
        String hoTen,
        String sdt,
        String trangThai,
        String emailHeThong,
        String username,
        String diaChi,
        LocalDate ngaySinh
) {
}

