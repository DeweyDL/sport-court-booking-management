package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateBookingRequest(
        String customerId,
        String employeeId,
        List<SelectedBookingSlot> selectedSlots,
        BigDecimal deposit,
        BigDecimal discount
) {
}
