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

    boolean updatePasswordByUsernameAndPhone(String username, String sdt, String passwordHash) throws SQLException;
}
