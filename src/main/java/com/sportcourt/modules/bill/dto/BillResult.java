package com.sportcourt.modules.bill.dto;

public record BillResult<T>(
        boolean success,
        String message,
        T data
) {
    public static <T> BillResult<T> ok(String message, T data) {
        return new BillResult<>(true, message, data);
    }

    public static <T> BillResult<T> fail(String message) {
        return new BillResult<>(false, message, null);
    }
}
