package com.sportcourt.auth.dto;

public record ResetPasswordRequest(
        String username,
        String email,
        String newPassword
) {
}
