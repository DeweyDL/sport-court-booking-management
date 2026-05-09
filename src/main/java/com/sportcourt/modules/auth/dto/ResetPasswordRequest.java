package com.sportcourt.modules.auth.dto;

public record ResetPasswordRequest(
        String username,
        String email,
        String newPassword
) {
}
