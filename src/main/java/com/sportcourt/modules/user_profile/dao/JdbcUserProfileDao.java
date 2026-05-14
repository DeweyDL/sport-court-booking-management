package com.sportcourt.modules.user_profile.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.user_profile.dto.UpdateUserProfileRequest;
import com.sportcourt.modules.user_profile.dto.UserProfileDto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

public class JdbcUserProfileDao implements UserProfileDao {
    @Override
    public Optional<UserProfileDto> findByAccount(String userId, String accountId) throws SQLException {
        String sql = """
                SELECT u.USER_ID,
                       a.ACCOUNT_ID,
                       u.HOTEN,
                       u.SDT,
                       u.EMAIL,
                       u.NGAYSINH,
                       u.DIACHI,
                       COALESCE(rg.GROUP_NAME, lnv.VITRI, 'Người dùng') AS ROLE_NAME,
                       hkh.TEN_HANG AS CUSTOMER_RANK
                FROM USERS u
                JOIN ACCOUNT a
                  ON a.USER_ID = u.USER_ID
                 AND a.IS_DELETED = 0
                LEFT JOIN (
                    SELECT account_id, group_id
                    FROM (
                        SELECT arg.ACCOUNT_ID,
                               arg.GROUP_ID,
                               ROW_NUMBER() OVER (
                                   PARTITION BY arg.ACCOUNT_ID
                                   ORDER BY arg.CREATED_AT DESC, arg.ACCOUNT_ROLE_GROUP_ID DESC
                               ) AS rn
                        FROM ACCOUNT_ROLE_GROUP arg
                        WHERE arg.IS_DELETED = 0
                    )
                    WHERE rn = 1
                ) arg_latest
                  ON arg_latest.ACCOUNT_ID = a.ACCOUNT_ID
                LEFT JOIN ROLE_GROUP rg
                  ON rg.GROUP_ID = arg_latest.GROUP_ID
                 AND rg.IS_DELETED = 0
                LEFT JOIN KHACH_HANG kh
                  ON kh.USER_ID = u.USER_ID
                 AND kh.IS_DELETED = 0
                LEFT JOIN HANG_KHACH_HANG hkh
                  ON hkh.MA_HANG = kh.MA_HANG
                 AND hkh.IS_DELETED = 0
                LEFT JOIN NHAN_VIEN nv
                  ON nv.USER_ID = u.USER_ID
                 AND nv.IS_DELETED = 0
                LEFT JOIN LOAI_NHAN_VIEN lnv
                  ON lnv.MALNV = nv.MALNV
                 AND lnv.IS_DELETED = 0
                WHERE u.USER_ID = ?
                  AND a.ACCOUNT_ID = ?
                  AND u.IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, accountId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserProfileDto(
                        rs.getString("USER_ID"),
                        rs.getString("ACCOUNT_ID"),
                        rs.getString("HOTEN"),
                        rs.getString("SDT"),
                        rs.getString("EMAIL"),
                        toLocalDate(rs.getDate("NGAYSINH")),
                        rs.getString("DIACHI"),
                        rs.getString("ROLE_NAME"),
                        rs.getString("CUSTOMER_RANK")
                ));
            }
        }
    }

    @Override
    public boolean updateProfile(String userId, String accountId, UpdateUserProfileRequest request) throws SQLException {
        String updateUserSql = """
                UPDATE USERS
                SET HOTEN = ?, SDT = ?, EMAIL = ?, NGAYSINH = ?, DIACHI = ?
                WHERE USER_ID = ?
                  AND IS_DELETED = 0
                """;
        String updateAccountSql = """
                UPDATE ACCOUNT
                SET USERNAME = ?
                WHERE ACCOUNT_ID = ?
                  AND USER_ID = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStatement = connection.prepareStatement(updateUserSql);
                 PreparedStatement accountStatement = connection.prepareStatement(updateAccountSql)) {
                userStatement.setString(1, request.fullName());
                userStatement.setString(2, request.phoneNumber());
                setNullableString(userStatement, 3, request.email());
                if (request.birthDate() == null) {
                    userStatement.setNull(4, Types.DATE);
                } else {
                    userStatement.setDate(4, Date.valueOf(request.birthDate()));
                }
                setNullableString(userStatement, 5, request.address());
                userStatement.setString(6, userId);
                int userUpdated = userStatement.executeUpdate();

                accountStatement.setString(1, request.phoneNumber());
                accountStatement.setString(2, accountId);
                accountStatement.setString(3, userId);
                int accountUpdated = accountStatement.executeUpdate();

                if (userUpdated <= 0 || accountUpdated <= 0) {
                    connection.rollback();
                    return false;
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public Optional<String> findPasswordHash(String userId, String accountId) throws SQLException {
        String sql = """
                SELECT PASSWORD_HASH
                FROM ACCOUNT
                WHERE USER_ID = ?
                  AND ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, accountId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(rs.getString("PASSWORD_HASH"));
            }
        }
    }

    @Override
    public boolean updatePasswordHash(String userId, String accountId, String passwordHash) throws SQLException {
        String sql = """
                UPDATE ACCOUNT
                SET PASSWORD_HASH = ?
                WHERE USER_ID = ?
                  AND ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setString(2, userId);
            statement.setString(3, accountId);
            return statement.executeUpdate() > 0;
        }
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static java.time.LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}
