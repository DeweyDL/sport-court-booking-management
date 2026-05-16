package com.sportcourt.modules.booking_management.dto;

public record BookingBranchOption(
        String branchId,
        String branchName
) {
    @Override
    public String toString() {
        if (branchName == null || branchName.isBlank()) {
            return branchId;
        }
        return branchName + " (" + branchId + ")";
    }
    public String branchId() { return branchId; }

}

