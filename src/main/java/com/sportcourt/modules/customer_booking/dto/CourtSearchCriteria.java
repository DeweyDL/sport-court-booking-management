package com.sportcourt.modules.customer_booking.dto;

public record CourtSearchCriteria(
        String keyword,
        String branchId,
        String sportTypeId,
        CourtSortBy sortBy,
        String sortDirection
) {
}

enum CourtSortBy {
    PRICE,
    COURT_NAME,
    BRANCH_NAME
}

enum SortDirection {
    ASC,
    DESC
}
