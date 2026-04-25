package com.sportcourt.auth.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String password,
        String hoTen,
        String sdt,
        String email
) {
}
