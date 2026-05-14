package com.sportcourt.modules.user_profile.dto;

public record UserProfileResult<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> UserProfileResult<T> ok(String message, T data) {
        return new UserProfileResult<>(true, message, data);
    }

    public static <T> UserProfileResult<T> fail(String message) {
        return new UserProfileResult<>(false, message, null);
    }
}
