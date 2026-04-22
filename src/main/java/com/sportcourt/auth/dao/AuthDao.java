package com.sportcourt.auth.dao;

import com.sportcourt.auth.dto.AuthPrincipal;
import com.sportcourt.auth.dto.RegisterRequest;

import java.sql.SQLException;
import java.util.Optional;

public interface AuthDao {
    Optional<AuthPrincipal> findPrincipalByUsername(String username) throws SQLException;

    Optional<String> findPasswordHashByUsername(String username) throws SQLException;

    boolean existsUsername(String username) throws SQLException;

    boolean existsEmail(String email) throws SQLException;

    boolean existsPhone(String sdt) throws SQLException;

    void createUserAndAccount(String userId, String accountId, RegisterRequest request, String passwordHash) throws SQLException;

    boolean updatePasswordByUsernameAndEmail(String username, String email, String passwordHash) throws SQLException;

    Optional<String> findEmailByUsernameAndEmail(String username, String email) throws SQLException;

    void createEmailOtp(String otpId, String email, String otpCode, String purpose, int expireMinutes) throws SQLException;

    boolean consumeValidOtp(String email, String otpCode, String purpose) throws SQLException;

    boolean hasVerifiedOtp(String email, String purpose, int validWindowMinutes) throws SQLException;
}
