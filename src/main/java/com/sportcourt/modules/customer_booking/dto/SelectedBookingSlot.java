package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SelectedBookingSlot(
        String courtId,
        String priceBoardId,
        LocalDate bookingDate,
        BigDecimal unitPrice
) {
}
