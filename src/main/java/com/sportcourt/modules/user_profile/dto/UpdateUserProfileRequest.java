package com.sportcourt.modules.user_profile.dto;

import java.time.LocalDate;

public record UpdateUserProfileRequest(
        String fullName,
        String phoneNumber,
        String email,
        LocalDate birthDate,
        String address
) {
}
