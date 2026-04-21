package com.sportcourt.auth.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String username,
        String password,
        String hoTen,
        String sdt,
        String email,
        LocalDate ngaySinh,
        String diaChi
) {
}
