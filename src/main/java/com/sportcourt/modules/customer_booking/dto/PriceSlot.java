package com.sportcourt.modules.customer_booking.dto;

import java.math.BigDecimal;

public record PriceSlot(
        String priceBoardId,
        String areaId,
        int startHour,
        int endHour,
        BigDecimal price
) {
}
