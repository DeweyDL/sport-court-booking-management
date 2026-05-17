package com.sportcourt.modules.booking_management.dto;

public record BookingOpenHours(int startHourInclusive, int endHourExclusive) {
    public static BookingOpenHours defaultHours() {
        return fullDay();
    }

    public static BookingOpenHours fullDay() {
        return new BookingOpenHours(0, 24);
    }
}

