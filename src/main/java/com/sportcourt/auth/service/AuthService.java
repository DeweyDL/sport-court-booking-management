package com.sportcourt.auth.service;

import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.auth.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResult login(LoginRequest request);

    AuthResult register(RegisterRequest request);

    AuthResult resetPassword(ResetPasswordRequest request);
}
