package com.sportcourt.auth.service;

import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.auth.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResult login(LoginRequest request);

    AuthResult register(RegisterRequest request);

    AuthResult resetPassword(ResetPasswordRequest request);

    AuthResult sendRegisterOtp(String email);

    AuthResult verifyRegisterOtp(String email, String otpCode);

    AuthResult sendResetPasswordOtp(String username, String email);

    AuthResult verifyResetPasswordOtp(String username, String email, String otpCode);
}
