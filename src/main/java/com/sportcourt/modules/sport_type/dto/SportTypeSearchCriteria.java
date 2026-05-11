package com.sportcourt.modules.sport_type.dto;

import java.time.LocalDateTime;

public record SportTypeSearchCriteria(String sportId,
                                      String name,
                                      String description,
                                      LocalDateTime createdAt,
                                      String keyword,
                                      String sortBy,
                                      String sortDirection) {
    public String getKeyword() {
        return keyword;
    }
}
