package com.sportcourt.modules.user_profile.controller;

import com.sportcourt.modules.user_profile.dto.ChangePasswordRequest;
import com.sportcourt.modules.user_profile.dto.UpdateUserProfileRequest;
import com.sportcourt.modules.user_profile.dto.UserProfileDto;
import com.sportcourt.modules.user_profile.dto.UserProfileResult;
import com.sportcourt.modules.user_profile.service.UserProfileService;
import com.sportcourt.modules.user_profile.service.UserProfileServiceImpl;

public class UserProfileController {
    private final UserProfileService service;

    public UserProfileController() {
        this(new UserProfileServiceImpl());
    }

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    public UserProfileResult<UserProfileDto> getCurrentProfile() {
        return service.getCurrentProfile();
    }

    public UserProfileResult<UserProfileDto> updateCurrentProfile(UpdateUserProfileRequest request) {
        return service.updateCurrentProfile(request);
    }

    public UserProfileResult<Void> changePassword(ChangePasswordRequest request) {
        return service.changePassword(request);
    }
}
