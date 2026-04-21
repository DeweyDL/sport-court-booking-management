package auth.model;

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
