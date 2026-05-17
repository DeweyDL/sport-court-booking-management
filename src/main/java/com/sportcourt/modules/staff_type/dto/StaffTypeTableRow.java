package com.sportcourt.modules.staff_type.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StaffTypeTableRow(
        String staffTypeId,
        String position,
        BigDecimal salary,
        LocalDateTime createdAt
) {}
