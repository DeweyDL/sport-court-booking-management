package com.sportcourt.auth.dto;

public record ResetPasswordRequest(
        String username,
        String sdt,
        String newPassword
) {
}
