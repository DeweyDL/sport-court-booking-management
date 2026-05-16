package com.sportcourt.modules.booking_management.dto;

import java.time.LocalDate;

public record BookingPitchDetailDTO(
        String courtId,
        int startHour,
        int endHour,
        LocalDate bookingDate,
        double price
) {
}