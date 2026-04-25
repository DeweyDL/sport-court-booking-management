package com.sportcourt.modules.auth.service;

import com.sportcourt.modules.auth.dao.AuthDao;
import com.sportcourt.modules.auth.dao.JdbcAuthDao;
import com.sportcourt.modules.auth.dto.AuthPrincipal;
import com.sportcourt.modules.auth.dto.AuthResult;
import com.sportcourt.modules.auth.dto.LoginRequest;
import com.sportcourt.modules.auth.dto.RegisterRequest;
import com.sportcourt.modules.auth.dto.ResetPasswordRequest;
import com.sportcourt.modules.auth.util.MailSender;
import com.sportcourt.modules.auth.util.Sha256Password;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {
    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int OTP_VERIFIED_WINDOW_MINUTES = 10;
    private static final int SESSION_EXPIRE_MINUTES = 120;

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
        if (request == null) {
            return AuthResult.fail("Dữ liệu đăng nhập không hợp lệ.");
        }

        try {
            String phone = normalize(request.phone());
            String password = normalize(request.password());
            Optional<String> passwordHashOpt = authDao.findPasswordHashByPhone(phone);
            if (passwordHashOpt.isEmpty() || !Sha256Password.matches(password, passwordHashOpt.get())) {
                return AuthResult.fail("Sai số điện thoại hoặc mật khẩu.");
            }

            Optional<AuthPrincipal> principal = authDao.findPrincipalByPhone(phone);
            if (principal.isEmpty()) {
                return AuthResult.fail("Không tìm thấy thông tin người dùng.");
            }
            String sessionToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            authDao.createAccountToken(generateId("TOK"), principal.get().accountId(), sessionToken, SESSION_EXPIRE_MINUTES);
            return AuthResult.ok("Đăng nhập thành công.", principal.get());
        } catch (SQLException e) {
            return AuthResult.fail("Đăng nhập thất bại: " + mapOracleError(e));
        }
    }

    @Override
    public AuthResult register(RegisterRequest request) {
        if (request == null) {
            return AuthResult.fail("Dữ liệu đăng ký không hợp lệ.");
        }

        try {
            String userId = generateId("USR");
            String accountId = generateId("ACC");
            String customerId = generateId("KH");
            String password = normalize(request.password());
            String passwordHash = Sha256Password.hash(password);

            RegisterRequest normalized = new RegisterRequest(
                    password,
                    normalize(request.hoTen()),
                    normalize(request.sdt()),
                    normalize(request.email())

            );
            if (!authDao.hasVerifiedOtp(normalized.email(), PURPOSE_REGISTER, OTP_VERIFIED_WINDOW_MINUTES)) {
                return AuthResult.fail("Vui lòng xác thực OTP email trước khi đăng ký.");
            }

            authDao.createUserAndAccount(userId, accountId, customerId, normalized, passwordHash);
            return AuthResult.ok("Đăng ký thành công.", null);
        } catch (SQLException e) {
            return AuthResult.fail("Đăng ký thất bại: " + mapOracleError(e));
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
            return AuthResult.fail("Không thể đặt lại mật khẩu: " + mapOracleError(e));
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
            return AuthResult.fail("Không thể gửi OTP: " + mapOracleError(e));
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
            return AuthResult.fail("Không thể xác thực OTP: " + mapOracleError(e));
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
            return AuthResult.fail("Không thể gửi OTP: " + mapOracleError(e));
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
            return AuthResult.fail("Không thể xác thực OTP: " + mapOracleError(e));
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

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String mapOracleError(SQLException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toUpperCase();
        if (message.contains("UQ_USERS_SDT") || message.contains("UQ_ACCOUNT_USERNAME")) {
            return "Số điện thoại đã tồn tại.";
        }
        if (message.contains("UQ_USERS_EMAIL")) {
            return "Email đã tồn tại.";
        }
        if (message.contains("CK_USERS_SDT")) {
            return "Số điện thoại không đúng định dạng.";
        }
        if (message.contains("CK_USERS_EMAIL")) {
            return "Email không đúng định dạng.";
        }
        if (message.contains("ORA-01400")) {
            return "Thiếu thông tin bắt buộc.";
        }
        if (message.contains("ORA-02290")) {
            return "Dữ liệu không hợp lệ theo ràng buộc DB.";
        }
        if (message.contains("ORA-02291") || message.contains("ORA-02292")) {
            return "Lỗi ràng buộc khóa ngoại trong DB.";
        }
        if (message.contains("ORA-00001")) {
            return "Dữ liệu bị trùng (vi phạm UNIQUE).";
        }
        return ex.getMessage();
    }
}
