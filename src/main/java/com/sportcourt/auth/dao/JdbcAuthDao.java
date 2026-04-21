package com.sportcourt.auth.dao;

import com.sportcourt.auth.dto.AuthPrincipal;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.common.db.ConnectionUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

public class JdbcAuthDao implements AuthDao {

    @Override
    public Optional<AuthPrincipal> findPrincipalByUsername(String username) throws SQLException {
        String sql = """
                SELECT a.ACCOUNT_ID, a.USER_ID, a.USERNAME, a.STATUS, u.HOTEN, u.EMAIL, u.SDT
                FROM ACCOUNT a
                JOIN USERS u ON u.USER_ID = a.USER_ID
                WHERE a.USERNAME = ?
                  AND a.IS_DELETED = 0
                  AND u.IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AuthPrincipal(
                        rs.getString("ACCOUNT_ID"),
                        rs.getString("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("STATUS"),
                        rs.getString("HOTEN"),
                        rs.getString("EMAIL"),
                        rs.getString("SDT")
                ));
            }
        }
    }

    @Override
    public Optional<String> findPasswordHashByUsername(String username) throws SQLException {
        String sql = """
                SELECT PASSWORD_HASH
                FROM ACCOUNT
                WHERE USERNAME = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(rs.getString("PASSWORD_HASH"));
            }
        }
    }

    @Override
    public boolean existsUsername(String username) throws SQLException {
        return exists("SELECT 1 FROM ACCOUNT WHERE USERNAME = ? AND IS_DELETED = 0", username);
    }

    @Override
    public boolean existsEmail(String email) throws SQLException {
        return exists("SELECT 1 FROM USERS WHERE EMAIL = ? AND IS_DELETED = 0", email);
    }

    @Override
    public boolean existsPhone(String sdt) throws SQLException {
        return exists("SELECT 1 FROM USERS WHERE SDT = ? AND IS_DELETED = 0", sdt);
    }

    @Override
    public void createUserAndAccount(String userId, String accountId, RegisterRequest request, String passwordHash) throws SQLException {
        String insertUser = """
                INSERT INTO USERS(USER_ID, HOTEN, SDT, EMAIL, NGAYSINH, DIACHI, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertAccount = """
                INSERT INTO ACCOUNT(ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, 'ACTIVE', SYSDATE, 0)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStmt = connection.prepareStatement(insertUser);
                 PreparedStatement accountStmt = connection.prepareStatement(insertAccount)) {
                userStmt.setString(1, userId);
                userStmt.setString(2, request.hoTen());
                userStmt.setString(3, request.sdt());
                userStmt.setString(4, request.email());
                if (request.ngaySinh() != null) {
                    userStmt.setDate(5, Date.valueOf(request.ngaySinh()));
                } else {
                    userStmt.setNull(5, Types.DATE);
                }
                userStmt.setString(6, request.diaChi());
                userStmt.executeUpdate();

                accountStmt.setString(1, accountId);
                accountStmt.setString(2, userId);
                accountStmt.setString(3, request.username());
                accountStmt.setString(4, passwordHash);
                accountStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean updatePasswordByUsernameAndPhone(String username, String sdt, String passwordHash) throws SQLException {
        String sql = """
                UPDATE ACCOUNT a
                SET a.PASSWORD_HASH = ?
                WHERE a.USERNAME = ?
                  AND a.USER_ID IN (
                      SELECT u.USER_ID
                      FROM USERS u
                      WHERE u.SDT = ?
                        AND u.IS_DELETED = 0
                  )
                  AND a.IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, username);
            statement.setString(3, sdt);
            return statement.executeUpdate() > 0;
        }
    }

    private boolean exists(String sql, String value) throws SQLException {
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
