package com.sportcourt.modules.booking_management.dto;

import java.util.List;

public record BookingInvoiceDTO(
        String invoiceId,
        String customerName,
        String customerPhone,
        String branchName,
        String branchAddress,
        double totalAmount,
        String status,
        List<BookingPitchDetailDTO> pitches
) {}