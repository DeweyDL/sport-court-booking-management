package com.sportcourt.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
