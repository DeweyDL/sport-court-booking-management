package com.sportcourt.modules.customer.dto;

public record CreateCustomerRequest(
        String maKhachHang,
        String hoTen,
        String sdt
) {
    public CreateCustomerRequest(String hoTen, String sdt) {
        this(null, hoTen, sdt);
    }
}

