package com.sportcourt.modules.user_profile.dto;

import java.time.LocalDate;

public record UserProfileDto(
        String userId,
        String accountId,
        String fullName,
        String phoneNumber,
        String email,
        LocalDate birthDate,
        String address,
        String roleName,
        String customerRank
) {
}
