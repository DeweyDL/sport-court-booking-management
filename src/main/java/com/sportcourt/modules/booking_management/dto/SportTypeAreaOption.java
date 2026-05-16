package com.sportcourt.modules.booking_management.dto;

import java.util.List;
import java.util.stream.Collectors;

public record SportTypeAreaOption(
        String sportTypeId,
        String sportTypeName,
        List<String> areaIds
) {
    @Override
    public String toString() {
        if (areaIds == null || areaIds.isEmpty()) {
            return sportTypeName;
        }
        String areas = areaIds.stream().collect(Collectors.joining(", "));
        return sportTypeName + " (" + areas + ")";
    }
}
