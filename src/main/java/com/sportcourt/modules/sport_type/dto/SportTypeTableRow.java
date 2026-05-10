package com.sportcourt.modules.sport_type.dto;

import java.time.LocalDateTime;

public record SportTypeTableRow (
    String sportId,
    String name,
    String description,
    LocalDateTime createdAt
) {}
