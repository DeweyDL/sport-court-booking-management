package com.sportcourt.modules.auth.dto;

public record LoginRequest(
        String phone,
        String password
) {
}
