package com.sportcourt.modules.auth.controller;

import com.sportcourt.modules.auth.dto.AuthResult;
import com.sportcourt.modules.auth.dto.LoginRequest;
import com.sportcourt.modules.auth.dto.RegisterRequest;
import com.sportcourt.modules.auth.dto.ResetPasswordRequest;
import com.sportcourt.modules.auth.service.AuthService;
import com.sportcourt.modules.auth.service.AuthServiceImpl;

public class AuthController {
    private final AuthService authService;

    public AuthController() {
        this(new AuthServiceImpl());
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public AuthResult login(LoginRequest request) {
        return authService.login(request);
    }

    public AuthResult register(RegisterRequest request) {
        return authService.register(request);
    }

    public AuthResult resetPassword(ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    public AuthResult sendRegisterOtp(String email) {
        return authService.sendRegisterOtp(email);
    }

    public AuthResult verifyRegisterOtp(String email, String otpCode) {
        return authService.verifyRegisterOtp(email, otpCode);
    }

    public AuthResult sendResetPasswordOtp(String username, String email) {
        return authService.sendResetPasswordOtp(username, email);
    }

    public AuthResult verifyResetPasswordOtp(String username, String email, String otpCode) {
        return authService.verifyResetPasswordOtp(username, email, otpCode);
    }
}
