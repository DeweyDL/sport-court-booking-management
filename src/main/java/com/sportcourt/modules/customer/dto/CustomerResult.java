package com.sportcourt.modules.customer.dto;

public record CustomerResult<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> CustomerResult<T> ok(String message, T data) {
        return new CustomerResult<>(true, message, data);
    }

    public static <T> CustomerResult<T> fail(String message) {
        return new CustomerResult<>(false, message, null);
    }
}

