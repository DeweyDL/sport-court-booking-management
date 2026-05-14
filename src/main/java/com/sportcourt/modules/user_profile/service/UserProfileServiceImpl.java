package com.sportcourt.modules.user_profile.service;

import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.auth.util.Sha256Password;
import com.sportcourt.modules.user_profile.dao.JdbcUserProfileDao;
import com.sportcourt.modules.user_profile.dao.UserProfileDao;
import com.sportcourt.modules.user_profile.dto.ChangePasswordRequest;
import com.sportcourt.modules.user_profile.dto.UpdateUserProfileRequest;
import com.sportcourt.modules.user_profile.dto.UserProfileDto;
import com.sportcourt.modules.user_profile.dto.UserProfileResult;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserProfileServiceImpl implements UserProfileService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserProfileDao userProfileDao;

    public UserProfileServiceImpl() {
        this(new JdbcUserProfileDao());
    }

    public UserProfileServiceImpl(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    @Override
    public UserProfileResult<UserProfileDto> getCurrentProfile() {
        try {
            UserSession session = SessionManager.requireSession();
            Optional<UserProfileDto> profile = userProfileDao.findByAccount(session.getUserId(), session.getAccountId());
            if (profile.isEmpty()) {
                return UserProfileResult.fail("Không tìm thấy thông tin cá nhân.");
            }
            return UserProfileResult.ok("Lấy thông tin cá nhân thành công.", profile.get());
        } catch (IllegalStateException e) {
            return UserProfileResult.fail(e.getMessage());
        } catch (SQLException e) {
            return UserProfileResult.fail("Không thể lấy thông tin cá nhân: " + mapOracleError(e));
        }
    }

    @Override
    public UserProfileResult<UserProfileDto> updateCurrentProfile(UpdateUserProfileRequest request) {
        if (request == null) {
            return UserProfileResult.fail("Chưa nhập thông tin cập nhật.");
        }
        if (isBlank(request.fullName())) {
            return UserProfileResult.fail("Chưa nhập họ tên.");
        }
        if (isBlank(request.phoneNumber())) {
            return UserProfileResult.fail("Chưa nhập số điện thoại.");
        }
        String phone = request.phoneNumber().trim();
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return UserProfileResult.fail("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        }
        String email = normalizeOptional(request.email());
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return UserProfileResult.fail("Email không đúng định dạng.");
        }

        try {
            UserSession session = SessionManager.requireSession();
            UpdateUserProfileRequest normalized = new UpdateUserProfileRequest(
                    request.fullName().trim(),
                    phone,
                    email,
                    request.birthDate(),
                    normalizeOptional(request.address())
            );
            boolean updated = userProfileDao.updateProfile(session.getUserId(), session.getAccountId(), normalized);
            if (!updated) {
                return UserProfileResult.fail("Không tìm thấy tài khoản để cập nhật.");
            }
            return getCurrentProfile();
        } catch (IllegalStateException e) {
            return UserProfileResult.fail(e.getMessage());
        } catch (SQLException e) {
            return UserProfileResult.fail("Không thể cập nhật thông tin cá nhân: " + mapOracleError(e));
        }
    }

    @Override
    public UserProfileResult<Void> changePassword(ChangePasswordRequest request) {
        if (request == null) {
            return UserProfileResult.fail("Chưa nhập thông tin đổi mật khẩu.");
        }

        String currentPassword = normalizeOptional(request.currentPassword());
        String newPassword = normalizeOptional(request.newPassword());
        String confirmNewPassword = normalizeOptional(request.confirmNewPassword());

        if (isBlank(currentPassword)) {
            return UserProfileResult.fail("Chưa nhập mật khẩu hiện tại.");
        }
        if (isBlank(newPassword)) {
            return UserProfileResult.fail("Chưa nhập mật khẩu mới.");
        }
        if (isBlank(confirmNewPassword)) {
            return UserProfileResult.fail("Chưa nhập xác nhận mật khẩu mới.");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            return UserProfileResult.fail("Mật khẩu xác nhận không khớp.");
        }
        if (currentPassword.equals(newPassword)) {
            return UserProfileResult.fail("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }

        try {
            UserSession session = SessionManager.requireSession();
            Optional<String> passwordHash = userProfileDao.findPasswordHash(session.getUserId(), session.getAccountId());
            if (passwordHash.isEmpty() || !Sha256Password.matches(currentPassword, passwordHash.get())) {
                return UserProfileResult.fail("Mật khẩu hiện tại không đúng.");
            }

            boolean updated = userProfileDao.updatePasswordHash(
                    session.getUserId(),
                    session.getAccountId(),
                    Sha256Password.hash(newPassword)
            );
            if (!updated) {
                return UserProfileResult.fail("Không tìm thấy tài khoản để đổi mật khẩu.");
            }
            return UserProfileResult.ok("Đổi mật khẩu thành công.", null);
        } catch (IllegalStateException e) {
            return UserProfileResult.fail(e.getMessage());
        } catch (SQLException e) {
            return UserProfileResult.fail("Không thể đổi mật khẩu: " + mapOracleError(e));
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String mapOracleError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Lỗi cơ sở dữ liệu không xác định.";
        }
        String upperMessage = message.toUpperCase();
        if (upperMessage.contains("ORA-00001")) {
            if (upperMessage.contains("UQ_USERS_SDT") || upperMessage.contains("UQ_ACCOUNT_USERNAME")) {
                return "Số điện thoại đã tồn tại.";
            }
            if (upperMessage.contains("UQ_USERS_EMAIL")) {
                return "Email đã tồn tại.";
            }
            return "Dữ liệu đã tồn tại.";
        }
        if (upperMessage.contains("ORA-02290")) {
            if (upperMessage.contains("CK_USERS_SDT")) {
                return "Số điện thoại sai định dạng.";
            }
            if (upperMessage.contains("CK_USERS_EMAIL")) {
                return "Email sai định dạng.";
            }
            return "Dữ liệu sai định dạng.";
        }
        if (upperMessage.contains("ORA-12899")) {
            return "Dữ liệu vượt quá độ dài cho phép.";
        }
        return message;
    }
}
