package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingCourtLine(
        String courtId,
        String sportTypeName,
        LocalDateTime bookingDate,
        String startHour,
        String endHour,
        BigDecimal unitPrice,
        String detailStatus
) {
}
