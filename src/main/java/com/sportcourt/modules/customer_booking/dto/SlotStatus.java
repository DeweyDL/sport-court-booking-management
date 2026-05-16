package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SlotStatus(
        String courtId,
        String priceBoardId,
        LocalDate bookingDate,
        int startHour,
        int endHour,
        BigDecimal price,
        BookingSlotStatus status
) {
}
