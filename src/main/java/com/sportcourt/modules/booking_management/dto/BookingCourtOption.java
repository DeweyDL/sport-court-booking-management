package com.sportcourt.modules.booking_management.dto;

public record BookingCourtOption(String courtId) {
    @Override
    public String toString() {
        return courtId;
    }
}

