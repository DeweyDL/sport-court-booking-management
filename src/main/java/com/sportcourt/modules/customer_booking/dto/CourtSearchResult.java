package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;

public record CourtSearchResult(
        String courtId,
        String areaId,
        String branchAddress,
        String sportTypeId,
        String sportTypeName,
        BigDecimal minPrice,
        String courtStatus
) {
}
