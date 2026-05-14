package com.sportcourt.modules.user_profile.service;

import com.sportcourt.modules.user_profile.dto.ChangePasswordRequest;
import com.sportcourt.modules.user_profile.dto.UpdateUserProfileRequest;
import com.sportcourt.modules.user_profile.dto.UserProfileDto;
import com.sportcourt.modules.user_profile.dto.UserProfileResult;

public interface UserProfileService {
    UserProfileResult<UserProfileDto> getCurrentProfile();

    UserProfileResult<UserProfileDto> updateCurrentProfile(UpdateUserProfileRequest request);

    UserProfileResult<Void> changePassword(ChangePasswordRequest request);
}
