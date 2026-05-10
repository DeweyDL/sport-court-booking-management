package com.sportcourt.modules.sport_type.dto;

public record SportTypeForm (
        String sportId,
        String name,
        String description
) {
    public String getSportId() {
        return sportId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
