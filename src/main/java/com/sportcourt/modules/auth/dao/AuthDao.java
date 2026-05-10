package com.sportcourt.modules.auth.dao;

import com.sportcourt.modules.auth.dto.AuthPrincipal;
import com.sportcourt.modules.auth.dto.RegisterRequest;

import java.sql.SQLException;
import java.util.Optional;

public interface AuthDao {
    Optional<AuthPrincipal> findPrincipalByPhone(String phone) throws SQLException;

    Optional<String> findPasswordHashByPhone(String phone) throws SQLException;

    boolean existsEmail(String email) throws SQLException;

    int countCustomers() throws SQLException;

    void createUserAndAccount(String userId,
                              String accountId,
                              String customerId,
                              String accountRoleGroupId,
                              RegisterRequest request,
                              String passwordHash) throws SQLException;

    boolean updatePasswordByUsernameAndEmail(String username, String email, String passwordHash) throws SQLException;

    Optional<String> findEmailByUsernameAndEmail(String username, String email) throws SQLException;

    void createEmailOtp(String otpId, String email, String otpCode, String purpose, int expireMinutes) throws SQLException;

    boolean consumeValidOtp(String email, String otpCode, String purpose) throws SQLException;

    boolean hasVerifiedOtp(String email, String purpose, int validWindowMinutes) throws SQLException;

    void createAccountToken(String tokenId, String accountId, String tokenValue, int expireMinutes) throws SQLException;
}
