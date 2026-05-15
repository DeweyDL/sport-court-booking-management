package com.sportcourt.modules.staff_type.dto;

import java.time.LocalDateTime;

public record StaffTypeSearchCriteria(
        String staffTypeId,
        String position,
        String keyword,
        String sortBy,
        String sortDirection
) {
    public String getKeyword() {
        return keyword;
    }
}
