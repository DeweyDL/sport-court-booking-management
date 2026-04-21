package com.sportcourt.auth.service;

import com.sportcourt.auth.dao.AuthDao;
import com.sportcourt.auth.dao.JdbcAuthDao;
import com.sportcourt.auth.dto.AuthPrincipal;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.auth.dto.ResetPasswordRequest;
import com.sportcourt.auth.util.Sha256Password;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {
    private final AuthDao authDao;

    public AuthServiceImpl() {
        this(new JdbcAuthDao());
    }

    public AuthServiceImpl(AuthDao authDao) {
        this.authDao = authDao;
    }

    @Override
    public AuthResult login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return AuthResult.fail("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
        }

        try {
            Optional<String> passwordHashOpt = authDao.findPasswordHashByUsername(request.username().trim());
            if (passwordHashOpt.isEmpty() || !Sha256Password.matches(request.password(), passwordHashOpt.get())) {
                return AuthResult.fail("Sai tên đăng nhập hoặc mật khẩu.");
            }

            Optional<AuthPrincipal> principal = authDao.findPrincipalByUsername(request.username().trim());
            if (principal.isEmpty()) {
                return AuthResult.fail("Không tìm thấy thông tin người dùng.");
            }
            return AuthResult.ok("Đăng nhập thành công.", principal.get());
        } catch (SQLException e) {
            return AuthResult.fail("Lỗi kết nối: " + e.getMessage());
        }
    }

    @Override
    public AuthResult register(RegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())
                || isBlank(request.hoTen()) || isBlank(request.sdt()) || isBlank(request.email())) {
            return AuthResult.fail("Vui lòng nhập đầy đủ thông tin bắt buộc.");
        }

        try {
            if (authDao.existsUsername(request.username().trim())) {
                return AuthResult.fail("Tên đăng nhập đã tồn tại.");
            }
            if (authDao.existsPhone(request.sdt().trim())) {
                return AuthResult.fail("Số điện thoại đã tồn tại.");
            }
            if (authDao.existsEmail(request.email().trim())) {
                return AuthResult.fail("Email đã tồn tại.");
            }

            String userId = generateId("USR");
            String accountId = generateId("ACC");
            String passwordHash = Sha256Password.hash(request.password());

            RegisterRequest normalized = new RegisterRequest(
                    request.username().trim(),
                    request.password(),
                    request.hoTen().trim(),
                    request.sdt().trim(),
                    request.email().trim(),
                    request.ngaySinh(),
                    request.diaChi() == null ? null : request.diaChi().trim()
            );

            authDao.createUserAndAccount(userId, accountId, normalized, passwordHash);
            return AuthResult.ok("Đăng ký thành công.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Đăng ký thất bại: " + e.getMessage());
        }
    }

    @Override
    public AuthResult resetPassword(ResetPasswordRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.sdt()) || isBlank(request.newPassword())) {
            return AuthResult.fail("Vui lòng nhập đầy đủ thông tin khôi phục.");
        }

        try {
            String passwordHash = Sha256Password.hash(request.newPassword());
            boolean updated = authDao.updatePasswordByUsernameAndPhone(
                    request.username().trim(),
                    request.sdt().trim(),
                    passwordHash
            );
            if (!updated) {
                return AuthResult.fail("Không tìm thấy tài khoản phù hợp.");
            }
            return AuthResult.ok("Đặt lại mật khẩu thành công.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Không thể đặt lại mật khẩu: " + e.getMessage());
        }
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
