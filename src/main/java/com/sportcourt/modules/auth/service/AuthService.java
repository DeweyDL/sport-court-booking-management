package com.sportcourt.modules.auth.service;

import com.sportcourt.modules.auth.dto.AuthResult;
import com.sportcourt.modules.auth.dto.LoginRequest;
import com.sportcourt.modules.auth.dto.RegisterRequest;
import com.sportcourt.modules.auth.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResult login(LoginRequest request);

    AuthResult register(RegisterRequest request);

    AuthResult resetPassword(ResetPasswordRequest request);

    AuthResult sendRegisterOtp(String email);

    AuthResult verifyRegisterOtp(String email, String otpCode);

    AuthResult sendResetPasswordOtp(String username, String email);

    AuthResult verifyResetPasswordOtp(String username, String email, String otpCode);
}
