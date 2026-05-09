package com.sportcourt.modules.auth.dto;

public record AuthPrincipal(
        String accountId,
        String userId,
        String username,
        String status,
        String hoTen,
        String email,
        String sdt
) {
}
