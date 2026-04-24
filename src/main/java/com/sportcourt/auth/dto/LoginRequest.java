package com.sportcourt.auth.dto;

public record LoginRequest(
        String phone,
        String password
) {
}
