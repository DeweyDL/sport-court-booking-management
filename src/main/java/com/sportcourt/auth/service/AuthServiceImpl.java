package com.sportcourt.auth.service;

import com.sportcourt.auth.dao.AuthDao;
import com.sportcourt.auth.dao.JdbcAuthDao;
import com.sportcourt.auth.dto.AuthPrincipal;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.auth.dto.ResetPasswordRequest;
import com.sportcourt.auth.util.MailSender;
import com.sportcourt.auth.util.Sha256Password;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {
    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int OTP_VERIFIED_WINDOW_MINUTES = 10;

    private final AuthDao authDao;
    private MailSender mailSender;

    public AuthServiceImpl() {
        this(new JdbcAuthDao(), new MailSender());
    }

    public AuthServiceImpl(AuthDao authDao) {
        this(authDao, new MailSender());
    }

    public AuthServiceImpl(AuthDao authDao, MailSender mailSender) {
        this.authDao = authDao;
        this.mailSender = mailSender;
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
            if (!authDao.hasVerifiedOtp(normalized.email(), PURPOSE_REGISTER, OTP_VERIFIED_WINDOW_MINUTES)) {
                return AuthResult.fail("Vui lòng xác thực OTP email trước khi đăng ký.");
            }

            authDao.createUserAndAccount(userId, accountId, normalized, passwordHash);
            return AuthResult.ok("Đăng ký thành công.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Đăng ký thất bại: " + e.getMessage());
        }
    }

    @Override
    public AuthResult resetPassword(ResetPasswordRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.email()) || isBlank(request.newPassword())) {
            return AuthResult.fail("Vui lòng nhập đầy đủ thông tin khôi phục.");
        }

        try {
            Optional<String> emailOpt = authDao.findEmailByUsernameAndEmail(request.username().trim(), request.email().trim());
            if (emailOpt.isEmpty()) {
                return AuthResult.fail("Không tìm thấy tài khoản phù hợp.");
            }
            if (!authDao.hasVerifiedOtp(emailOpt.get(), PURPOSE_RESET_PASSWORD, OTP_VERIFIED_WINDOW_MINUTES)) {
                return AuthResult.fail("Vui lòng xác thực OTP trước khi đặt lại mật khẩu.");
            }

            String passwordHash = Sha256Password.hash(request.newPassword());
            boolean updated = authDao.updatePasswordByUsernameAndEmail(
                    request.username().trim(),
                    request.email().trim(),
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

    @Override
    public AuthResult sendRegisterOtp(String email) {
        if (isBlank(email)) {
            return AuthResult.fail("Vui lòng nhập email.");
        }
        String normalizedEmail = email.trim();
        try {
            if (authDao.existsEmail(normalizedEmail)) {
                return AuthResult.fail("Email đã tồn tại.");
            }
            String otpCode = generateOtpCode();
            authDao.createEmailOtp(generateId("OTP"), normalizedEmail, otpCode, PURPOSE_REGISTER, OTP_EXPIRE_MINUTES);
            getMailSender().sendOtp(normalizedEmail, otpCode, "Đăng ký");
            return AuthResult.ok("Đã gửi OTP đến email của bạn.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Không thể gửi OTP: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.fail("Lỗi gửi email OTP: " + e.getMessage());
        }
    }

    @Override
    public AuthResult verifyRegisterOtp(String email, String otpCode) {
        if (isBlank(email) || isBlank(otpCode)) {
            return AuthResult.fail("Vui lòng nhập email và mã OTP.");
        }
        try {
            boolean verified = authDao.consumeValidOtp(email.trim(), otpCode.trim(), PURPOSE_REGISTER);
            if (!verified) {
                return AuthResult.fail("OTP không hợp lệ hoặc đã hết hạn.");
            }
            return AuthResult.ok("Xác thực OTP thành công.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Không thể xác thực OTP: " + e.getMessage());
        }
    }

    @Override
    public AuthResult sendResetPasswordOtp(String username, String email) {
        if (isBlank(username) || isBlank(email)) {
            return AuthResult.fail("Vui lòng nhập tên đăng nhập và email.");
        }
        try {
            Optional<String> emailOpt = authDao.findEmailByUsernameAndEmail(username.trim(), email.trim());
            if (emailOpt.isEmpty()) {
                return AuthResult.fail("Không tìm thấy tài khoản phù hợp.");
            }
            String accountEmail = emailOpt.get();
            String otpCode = generateOtpCode();
            authDao.createEmailOtp(generateId("OTP"), accountEmail, otpCode, PURPOSE_RESET_PASSWORD, OTP_EXPIRE_MINUTES);
            getMailSender().sendOtp(accountEmail, otpCode, "Quên mật khẩu");
            return AuthResult.ok("Đã gửi OTP về email đã đăng ký.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Không thể gửi OTP: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.fail("Lỗi gửi email OTP: " + e.getMessage());
        }
    }

    @Override
    public AuthResult verifyResetPasswordOtp(String username, String email, String otpCode) {
        if (isBlank(username) || isBlank(email) || isBlank(otpCode)) {
            return AuthResult.fail("Vui lòng nhập đầy đủ thông tin OTP.");
        }
        try {
            Optional<String> emailOpt = authDao.findEmailByUsernameAndEmail(username.trim(), email.trim());
            if (emailOpt.isEmpty()) {
                return AuthResult.fail("Không tìm thấy tài khoản phù hợp.");
            }
            boolean verified = authDao.consumeValidOtp(emailOpt.get(), otpCode.trim(), PURPOSE_RESET_PASSWORD);
            if (!verified) {
                return AuthResult.fail("OTP không hợp lệ hoặc đã hết hạn.");
            }
            return AuthResult.ok("Xác thực OTP thành công.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Không thể xác thực OTP: " + e.getMessage());
        }
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String generateOtpCode() {
        int value = new Random().nextInt(900000) + 100000;
        return String.valueOf(value);
    }

    private MailSender getMailSender() {
        if (mailSender == null) {
            mailSender = new MailSender();
        }
        return mailSender;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
