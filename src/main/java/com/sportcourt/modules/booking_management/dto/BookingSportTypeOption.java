package com.sportcourt.modules.booking_management.dto;

public record BookingSportTypeOption(
        String sportTypeId,
        String sportTypeName
) {
    @Override
    public String toString() {
        if (sportTypeName == null || sportTypeName.isBlank()) {
            return sportTypeId;
        }
        return sportTypeName + " (" + sportTypeId + ")";
    }
}

