package com.sportcourt.modules.staff_type.dto;

import java.math.BigDecimal;

public record StaffTypeForm(
        String staffTypeId,
        String position,
        BigDecimal salary
) {
    public String getStaffTypeId() {
        return staffTypeId;
    }

    public String getPosition() {
        return position;
    }

    public BigDecimal getSalary() {
        return salary;
    }
}
