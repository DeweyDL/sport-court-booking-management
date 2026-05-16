package com.sportcourt.modules.booking_management.dto;

public record BookingAreaOption(
        String areaId,
        String sportTypeId,
        String sportTypeName
) {
    @Override
    public String toString() {
        // Combo/menu display
        if (sportTypeName == null || sportTypeName.isBlank()) {
            return areaId;
        }
        return areaId + " - " + sportTypeName;
    }
}

