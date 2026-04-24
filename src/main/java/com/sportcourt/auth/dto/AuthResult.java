package com.sportcourt.auth.dto;

public record AuthResult(
        boolean success,
        String message,
        AuthPrincipal principal
) {
    public static AuthResult ok(String message, AuthPrincipal principal) {
        return new AuthResult(true, message, principal);
    }

    public static AuthResult fail(String message) {
        return new AuthResult(false, message, null);
    }
}
