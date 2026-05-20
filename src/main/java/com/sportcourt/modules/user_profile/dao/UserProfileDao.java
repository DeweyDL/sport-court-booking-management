package com.sportcourt.modules.user_profile.dao;

import com.sportcourt.modules.user_profile.dto.UpdateUserProfileRequest;
import com.sportcourt.modules.user_profile.dto.UserProfileDto;

import java.sql.SQLException;
import java.util.Optional;

public interface UserProfileDao {
    Optional<UserProfileDto> findByAccount(String userId, String accountId) throws SQLException;

    boolean updateProfile(String userId, String accountId, UpdateUserProfileRequest request) throws SQLException;

    Optional<String> findPasswordHash(String userId, String accountId) throws SQLException;

    boolean updatePasswordHash(String userId, String accountId, String passwordHash) throws SQLException;
}
