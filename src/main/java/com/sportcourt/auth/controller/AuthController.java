package com.sportcourt.auth.controller;

import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.auth.dto.ResetPasswordRequest;
import com.sportcourt.auth.service.AuthService;
import com.sportcourt.auth.service.AuthServiceImpl;

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
}
