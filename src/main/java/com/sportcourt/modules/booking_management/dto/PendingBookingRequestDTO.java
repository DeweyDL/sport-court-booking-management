package com.sportcourt.modules.booking_management.dto;

import java.time.LocalDate;

public record PendingBookingRequestDTO(
        String invoiceId,
        String bookingDetailId,
        String customerName,
        String customerPhone,
        String branchName,
        String sportTypeName,
        String courtSummary,
        LocalDate bookingDate,
        int startHour,
        int endHour,
        int slotCount,
        double totalAmount,
        double depositAmount,
        String status
) {
}
