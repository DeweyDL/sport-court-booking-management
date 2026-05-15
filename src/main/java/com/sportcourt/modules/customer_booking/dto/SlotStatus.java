package com.sportcourt.modules.customer_booking.dto;

import java.time.LocalDate;

public record SlotStatus(
        String courtId,
        Integer priceBoardId,
        LocalDate bookingDate,
        Integer startHour,
        Integer endHour,
        Integer price,
        BookingSlotStatus status
) {
}
