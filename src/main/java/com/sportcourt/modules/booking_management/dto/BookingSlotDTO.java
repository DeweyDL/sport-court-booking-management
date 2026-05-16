package com.sportcourt.modules.booking_management.dto;

import java.time.LocalDate;

public record BookingSlotDTO(
        String bookingDetailId,
        String invoiceId,
        String courtId,
        String areaId,
        String sportTypeName,
        String customerName,
        String customerPhone,
        int startHour,
        int endHour,
        LocalDate bookingDate
) {
    public int durationHours() {
        return Math.max(0, endHour - startHour);
    }

    public String bookingKey() {
        // unique enough for spanning/merge rendering across adjacent hour cells
        String base = bookingDetailId == null ? "" : bookingDetailId;
        return base + "|" + (courtId == null ? "" : courtId);
    }

}

