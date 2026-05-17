package com.sportcourt.modules.customer_booking.dto;

import java.util.List;

public record CourtSchedule(
        String courtId,
        String courtName,
        List<SlotStatus> slots
) {
}
