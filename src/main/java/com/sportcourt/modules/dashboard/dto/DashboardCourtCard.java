package com.sportcourt.modules.dashboard.dto;

import java.math.BigDecimal;

/**
 * Projection DTO used by Dashboard to render dynamic cards.
 * Includes court identifier (displayed as "ten san con"), branch address, and a representative price.
 */
public record DashboardCourtCard(
        String courtName,
        String branchAddress,
        BigDecimal price
) {
}

