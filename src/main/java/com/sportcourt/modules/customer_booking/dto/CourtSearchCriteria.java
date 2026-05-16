package com.sportcourt.modules.customer_booking.dto;

public record CourtSearchCriteria(
        String keyword,
        String branchId,
        String sportTypeId,
        CourtSortBy sortBy,
        String sortDirection
) {
}

enum SortDirection {
    ASC,
    DESC
}
