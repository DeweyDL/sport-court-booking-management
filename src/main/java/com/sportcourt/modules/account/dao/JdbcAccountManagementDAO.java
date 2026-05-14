package com.sportcourt.modules.account.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcAccountManagementDAO implements AccountManagementDAO {
    @Override
    public List<AccountRow> findAccounts(String keyword) throws SQLException {
        String sql = """
                SELECT a.ACCOUNT_ID,
                       a.USERNAME,
                       a.STATUS,
                       a.IS_DELETED,
                       a.CREATED_AT,
                       u.HOTEN,
                       u.SDT,
                       u.EMAIL,
                       arg_latest.GROUP_ID,
                       rg.GROUP_NAME
                FROM ACCOUNT a
                JOIN USERS u
                    ON u.USER_ID = a.USER_ID
                LEFT JOIN (
                    SELECT account_id, group_id
                    FROM (
                        SELECT arg.ACCOUNT_ID,
                               arg.GROUP_ID,
                               ROW_NUMBER() OVER (
                                   PARTITION BY arg.ACCOUNT_ID
                                   ORDER BY arg.IS_DELETED ASC, arg.CREATED_AT DESC, arg.ACCOUNT_ROLE_GROUP_ID DESC
                               ) AS RN
                        FROM ACCOUNT_ROLE_GROUP arg
                    )
                    WHERE RN = 1
                ) arg_latest
                    ON arg_latest.ACCOUNT_ID = a.ACCOUNT_ID
                LEFT JOIN ROLE_GROUP rg
                    ON rg.GROUP_ID = arg_latest.GROUP_ID
                    AND rg.IS_DELETED = 0
                WHERE (
                      ? IS NULL
                      OR UPPER(a.ACCOUNT_ID) LIKE ?
                      OR UPPER(a.USERNAME) LIKE ?
                      OR UPPER(u.HOTEN) LIKE ?
                      OR UPPER(u.SDT) LIKE ?
                  )
                ORDER BY a.CREATED_AT DESC, a.ACCOUNT_ID ASC
                """;
        String normalized = normalizeKeyword(keyword);
        List<AccountRow> rows = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalized);
            statement.setString(2, toLikeValue(normalized));
            statement.setString(3, toLikeValue(normalized));
            statement.setString(4, toLikeValue(normalized));
            statement.setString(5, toLikeValue(normalized));

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapAccountRow(rs));
                }
            }
        }
        return rows;
    }

    @Override
    public List<RoleGroupOption> findRoleGroupOptions() throws SQLException {
        String sql = """
                SELECT GROUP_ID, GROUP_NAME
                FROM ROLE_GROUP
                WHERE IS_DELETED = 0
                ORDER BY GROUP_ID ASC
                """;
        List<RoleGroupOption> options = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                options.add(new RoleGroupOption(
                        rs.getString("GROUP_ID"),
                        rs.getString("GROUP_NAME")
                ));
            }
        }
        return options;
    }

    @Override
    public void assignRoleGroup(String accountId, String groupId) throws SQLException {
        String softDeleteSql = """
                UPDATE ACCOUNT_ROLE_GROUP
                SET IS_DELETED = 1
                WHERE ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        String insertSql = """
                INSERT INTO ACCOUNT_ROLE_GROUP (
                    ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED
                ) VALUES (
                    ?, ?, ?, SYSDATE, 0
                )
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement softDeleteStatement = connection.prepareStatement(softDeleteSql)) {
                    softDeleteStatement.setString(1, accountId);
                    softDeleteStatement.executeUpdate();
                }

                String nextId = generateNextAccountRoleGroupId(connection);
                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                    insertStatement.setString(1, nextId);
                    insertStatement.setString(2, accountId);
                    insertStatement.setString(3, groupId);
                    insertStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public void createAccount(String userId, String accountId, AccountUpsertRequest request, String passwordHash) throws SQLException {
        String insertUserSql = """
                INSERT INTO USERS (USER_ID, HOTEN, SDT, EMAIL, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertAccountSql = """
                INSERT INTO ACCOUNT (ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertAccountRoleGroupSql = """
                INSERT INTO ACCOUNT_ROLE_GROUP (ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, SYSDATE, 0)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement userStatement = connection.prepareStatement(insertUserSql)) {
                    userStatement.setString(1, userId);
                    userStatement.setString(2, request.getDisplayName().trim());
                    userStatement.setString(3, request.getPhone().trim());
                    userStatement.setString(4, normalizeNullable(request.getEmail()));
                    userStatement.executeUpdate();
                }

                try (PreparedStatement accountStatement = connection.prepareStatement(insertAccountSql)) {
                    accountStatement.setString(1, accountId);
                    accountStatement.setString(2, userId);
                    accountStatement.setString(3, request.getUsername().trim());
                    accountStatement.setString(4, passwordHash);
                    accountStatement.setString(5, request.getStatus().trim().toUpperCase());
                    accountStatement.executeUpdate();
                }

                String nextArgId = generateNextAccountRoleGroupId(connection);
                try (PreparedStatement roleStatement = connection.prepareStatement(insertAccountRoleGroupSql)) {
                    roleStatement.setString(1, nextArgId);
                    roleStatement.setString(2, accountId);
                    roleStatement.setString(3, request.getRoleGroupId().trim());
                    roleStatement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean updateAccount(AccountUpsertRequest request) throws SQLException {
        String updateUserSql = """
                UPDATE USERS
                SET HOTEN = ?,
                    SDT = ?,
                    EMAIL = ?
                WHERE USER_ID = (
                    SELECT a.USER_ID
                    FROM ACCOUNT a
                    WHERE a.ACCOUNT_ID = ?
                      AND a.IS_DELETED = 0
                )
                AND IS_DELETED = 0
                """;
        String updateAccountSql = """
                UPDATE ACCOUNT
                SET USERNAME = ?,
                    STATUS = ?
                WHERE ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(updateUserSql)) {
                    statement.setString(1, request.getDisplayName().trim());
                    statement.setString(2, request.getPhone().trim());
                    statement.setString(3, normalizeNullable(request.getEmail()));
                    statement.setString(4, request.getAccountId().trim());
                    statement.executeUpdate();
                }

                int accountUpdatedRows;
                try (PreparedStatement statement = connection.prepareStatement(updateAccountSql)) {
                    statement.setString(1, request.getUsername().trim());
                    statement.setString(2, request.getStatus().trim().toUpperCase());
                    statement.setString(3, request.getAccountId().trim());
                    accountUpdatedRows = statement.executeUpdate();
                }

                assignRoleGroup(connection, request.getAccountId().trim(), request.getRoleGroupId().trim());
                connection.commit();
                return accountUpdatedRows > 0;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean softDeleteAccount(String accountId) throws SQLException {
        String softDeleteAccountSql = """
                UPDATE ACCOUNT
                SET IS_DELETED = 1
                WHERE ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        String softDeleteRoleGroupSql = """
                UPDATE ACCOUNT_ROLE_GROUP
                SET IS_DELETED = 1
                WHERE ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int affectedRows;
                try (PreparedStatement statement = connection.prepareStatement(softDeleteAccountSql)) {
                    statement.setString(1, accountId);
                    affectedRows = statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(softDeleteRoleGroupSql)) {
                    statement.setString(1, accountId);
                    statement.executeUpdate();
                }
                connection.commit();
                return affectedRows > 0;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean restoreAccount(String accountId) throws SQLException {
        String restoreAccountSql = """
                UPDATE ACCOUNT
                SET IS_DELETED = 0, STATUS = 'ACTIVE'
                WHERE ACCOUNT_ID = ?
                  AND IS_DELETED = 1
                """;
        String restoreUserSql = """
                UPDATE USERS
                SET IS_DELETED = 0
                WHERE USER_ID = (SELECT USER_ID FROM ACCOUNT WHERE ACCOUNT_ID = ?)
                """;
        String restoreRoleGroupSql = """
                UPDATE ACCOUNT_ROLE_GROUP
                SET IS_DELETED = 0
                WHERE ACCOUNT_ROLE_GROUP_ID = (
                    SELECT ACCOUNT_ROLE_GROUP_ID
                    FROM (
                        SELECT ACCOUNT_ROLE_GROUP_ID
                        FROM ACCOUNT_ROLE_GROUP
                        WHERE ACCOUNT_ID = ?
                        ORDER BY CREATED_AT DESC, ACCOUNT_ROLE_GROUP_ID DESC
                    )
                    WHERE ROWNUM = 1
                )
                """;
        String restoreRoleSql = """
                UPDATE ACCOUNT_ROLE
                SET IS_DELETED = 0
                WHERE ACCOUNT_ID = ?
                """;
        String restoreStaffSql = """
                UPDATE NHAN_VIEN
                SET IS_DELETED = 0, TRANG_THAI = 'ACTIVE'
                WHERE USER_ID = (SELECT USER_ID FROM ACCOUNT WHERE ACCOUNT_ID = ?)
                """;
        String restoreCustomerSql = """
                UPDATE KHACH_HANG
                SET IS_DELETED = 0, TRANGTHAI = 'ACTIVE'
                WHERE USER_ID = (SELECT USER_ID FROM ACCOUNT WHERE ACCOUNT_ID = ?)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int affectedRows;
                try (PreparedStatement statement = connection.prepareStatement(restoreAccountSql)) {
                    statement.setString(1, accountId);
                    affectedRows = statement.executeUpdate();
                }
                if (affectedRows > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(restoreUserSql)) {
                        statement.setString(1, accountId);
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement(restoreRoleGroupSql)) {
                        statement.setString(1, accountId);
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement(restoreRoleSql)) {
                        statement.setString(1, accountId);
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement(restoreStaffSql)) {
                        statement.setString(1, accountId);
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement(restoreCustomerSql)) {
                        statement.setString(1, accountId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return affectedRows > 0;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public String generatedNextId() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(SUBSTR(ACCOUNT_ID, 5))), 0) + 1
                FROM ACCOUNT
                WHERE REGEXP_LIKE(ACCOUNT_ID, '^ACC-[0-9]+$')
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "ACC-" + rs.getInt(1);
                }
                return "ACC-1";
            }
        }
    }

    private AccountRow mapAccountRow(ResultSet rs) throws SQLException {
        AccountRow row = new AccountRow();
        row.setAccountId(rs.getString("ACCOUNT_ID"));
        row.setUsername(rs.getString("USERNAME"));
        row.setStatus(rs.getString("STATUS"));
        row.setDisplayName(rs.getString("HOTEN"));
        row.setPhone(rs.getString("SDT"));
        row.setEmail(rs.getString("EMAIL"));
        row.setGroupId(rs.getString("GROUP_ID"));
        row.setGroupName(rs.getString("GROUP_NAME"));
        row.setDeleted(rs.getInt("IS_DELETED") == 1);
        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        LocalDateTime createdDateTime = createdAt == null ? null : createdAt.toLocalDateTime();
        row.setCreatedAt(createdDateTime);
        return row;
    }

    private String generateNextAccountRoleGroupId(Connection connection) throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCOUNT_ROLE_GROUP_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                FROM ACCOUNT_ROLE_GROUP
                WHERE REGEXP_LIKE(ACCOUNT_ROLE_GROUP_ID, '^ARG\\d+$')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return "ARG" + String.format("%04d", rs.getInt("NEXT_ID"));
            }
        }
        throw new SQLException("Không thể sinh mã ACCOUNT_ROLE_GROUP_ID.");
    }

    private void assignRoleGroup(Connection connection, String accountId, String groupId) throws SQLException {
        String softDeleteSql = """
                UPDATE ACCOUNT_ROLE_GROUP
                SET IS_DELETED = 1
                WHERE ACCOUNT_ID = ?
                  AND IS_DELETED = 0
                """;
        String insertSql = """
                INSERT INTO ACCOUNT_ROLE_GROUP (
                    ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED
                ) VALUES (
                    ?, ?, ?, SYSDATE, 0
                )
                """;
        try (PreparedStatement softDeleteStatement = connection.prepareStatement(softDeleteSql)) {
            softDeleteStatement.setString(1, accountId);
            softDeleteStatement.executeUpdate();
        }
        String nextId = generateNextAccountRoleGroupId(connection);
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            insertStatement.setString(1, nextId);
            insertStatement.setString(2, accountId);
            insertStatement.setString(3, groupId);
            insertStatement.executeUpdate();
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toLikeValue(String keyword) {
        return keyword == null ? null : "%" + keyword + "%";
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
