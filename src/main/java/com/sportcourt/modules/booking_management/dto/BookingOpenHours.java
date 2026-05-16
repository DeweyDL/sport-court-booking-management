package com.sportcourt.modules.booking_management.dto;

public record BookingOpenHours(int startHourInclusive, int endHourExclusive) {
    public static BookingOpenHours defaultHours() {
        return new BookingOpenHours(0, 23);
    }
}

