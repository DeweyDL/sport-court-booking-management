package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookingPreview(
        String invoiceId,
        String customerId,
        String customerName,
        String phoneNumber,
        String branchId,
        String branchName,
        String branchAddress,
        List<BookingCourtLine> courtLines,
        BigDecimal totalCourtPrice,
        BigDecimal deposit,
        BigDecimal discount,
        BigDecimal totalAmount,
        String  invoiceStatuts
) {
}
