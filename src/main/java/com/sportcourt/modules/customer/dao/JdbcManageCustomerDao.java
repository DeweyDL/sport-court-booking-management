package com.sportcourt.modules.customer.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcManageCustomerDao implements ManageCustomerDao {
    @Override
    public List<CustomerSummary> findByName(String keyword) throws SQLException {
        String sql = """
                SELECT kh.MAKH, kh.USER_ID, u.HOTEN, u.SDT, hkh.TEN_HANG AS HANG_KHACH_HANG, kh.TRANGTHAI, kh.DOANH_THU
                       , u.DIACHI, u.NGAYSINH
                FROM KHACH_HANG kh
                JOIN USERS u ON u.USER_ID = kh.USER_ID
                LEFT JOIN HANG_KHACH_HANG hkh ON hkh.MA_HANG = kh.MA_HANG
                WHERE (
                      UPPER(u.HOTEN) LIKE '%' || UPPER(?) || '%'
                      OR u.SDT LIKE '%' || ? || '%'
                  )
                ORDER BY u.HOTEN ASC
                """;
        List<CustomerSummary> result = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String normalizedKeyword = keyword == null ? "" : keyword.trim();
            statement.setString(1, normalizedKeyword);
            statement.setString(2, normalizedKeyword);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new CustomerSummary(
                            rs.getString("MAKH"),
                            rs.getString("USER_ID"),
                            rs.getString("HOTEN"),
                            rs.getString("SDT"),
                            rs.getString("DIACHI"),
                            toLocalDate(rs.getDate("NGAYSINH")),
                            rs.getString("HANG_KHACH_HANG"),
                            rs.getString("TRANGTHAI"),
                            rs.getBigDecimal("DOANH_THU")
                    ));
                }
            }
        }
        return result;
    }

    @Override
    public Optional<CustomerProfile> findProfileById(String maKhachHang) throws SQLException {
        String sql = """
                SELECT kh.MAKH, kh.USER_ID, a.ACCOUNT_ID, u.HOTEN, u.SDT, u.DIACHI, u.EMAIL, a.USERNAME,
                       u.NGAYSINH, kh.TRANGTHAI, kh.MA_HANG, kh.DOANH_THU
                FROM KHACH_HANG kh
                JOIN USERS u ON u.USER_ID = kh.USER_ID
                LEFT JOIN ACCOUNT a ON a.USER_ID = u.USER_ID AND a.IS_DELETED = 0
                WHERE kh.MAKH = ?
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKhachHang);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new CustomerProfile(
                        rs.getString("MAKH"),
                        rs.getString("USER_ID"),
                        rs.getString("ACCOUNT_ID"),
                        rs.getString("HOTEN"),
                        rs.getString("SDT"),
                        rs.getString("DIACHI"),
                        toLocalDate(rs.getDate("NGAYSINH")),
                        rs.getString("EMAIL"),
                        rs.getString("USERNAME"),
                        rs.getString("TRANGTHAI"),
                        rs.getString("MA_HANG"),
                        rs.getBigDecimal("DOANH_THU")
                ));
            }
        }
    }

    @Override
    public int countCustomers() throws SQLException {
        String sql = """
                SELECT COUNT(*) AS CUSTOMER_COUNT
                FROM KHACH_HANG
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("CUSTOMER_COUNT");
            }
        }
        throw new SQLException("Khong the dem so luong khach hang.");
    }

    @Override
    public String nextNumericId(String tableName, String idColumn, String prefix) throws SQLException {
        String sql = switch ((tableName + "." + idColumn).toUpperCase()) {
            case "USERS.USER_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(USER_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM USERS
                    WHERE USER_ID LIKE ?
                    """;
            case "ACCOUNT.ACCOUNT_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCOUNT_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM ACCOUNT
                    WHERE ACCOUNT_ID LIKE ?
                    """;
            case "KHACH_HANG.MAKH" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MAKH, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM KHACH_HANG
                    WHERE MAKH LIKE ?
                    """;
            case "ACCOUNT_ROLE_GROUP.ACCOUNT_ROLE_GROUP_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCOUNT_ROLE_GROUP_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM ACCOUNT_ROLE_GROUP
                    WHERE ACCOUNT_ROLE_GROUP_ID LIKE ?
                    """;
            default -> throw new SQLException("Khong ho tro sinh ID cho " + tableName + "." + idColumn);
        };

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, prefix + "%");
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return prefix + rs.getLong("NEXT_ID");
                }
            }
        }
        throw new SQLException("Khong the sinh ID moi cho " + tableName + "." + idColumn);
    }

    @Override
    public void createCustomer(String userId, String accountId, String maKhachHang, String accountRoleGroupId,
                               CreateCustomerRequest request, String passwordHash, String username) throws SQLException {
        String call = "{ call PRC_THEM_KHACH_HANG(?, ?, ?, ?, ?, ?, ?) }";
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (CallableStatement statement = connection.prepareCall(call)) {
                statement.setString(1, userId);
                statement.setString(2, maKhachHang);
                statement.setString(3, accountId);
                statement.setString(4, accountRoleGroupId);
                statement.setString(5, request.hoTen().trim());
                statement.setString(6, request.sdt().trim());
                statement.setString(7, passwordHash);
                statement.execute();
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
    public boolean updateCustomer(String maKhachHang, UpdateCustomerRequest request) throws SQLException {
        String updateUser = """
                UPDATE USERS u
                SET u.HOTEN = ?, u.SDT = ?, u.EMAIL = ?, u.DIACHI = ?, u.NGAYSINH = ?
                WHERE u.USER_ID = (
                    SELECT kh.USER_ID
                    FROM KHACH_HANG kh
                    WHERE kh.MAKH = ?
                      AND kh.IS_DELETED = 0
                )
                  AND u.IS_DELETED = 0
                """;
        String updateAccount = """
                UPDATE ACCOUNT a
                SET a.USERNAME = (
                    SELECT u.SDT
                    FROM USERS u
                    WHERE u.USER_ID = a.USER_ID
                      AND u.IS_DELETED = 0
                )
                WHERE a.USER_ID = (
                    SELECT kh.USER_ID
                    FROM KHACH_HANG kh
                    WHERE kh.MAKH = ?
                      AND kh.IS_DELETED = 0
                )
                  AND a.IS_DELETED = 0
                """;
        String updateCustomer = """
                UPDATE KHACH_HANG
                SET TRANGTHAI = ?
                WHERE MAKH = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStmt = connection.prepareStatement(updateUser);
                 PreparedStatement accountStmt = connection.prepareStatement(updateAccount);
                 PreparedStatement customerStmt = connection.prepareStatement(updateCustomer)) {
                userStmt.setString(1, request.hoTen().trim());
                userStmt.setString(2, request.sdt().trim());
                userStmt.setString(3, request.emailHeThong());
                userStmt.setString(4, request.diaChi());
                if (request.ngaySinh() == null) {
                    userStmt.setNull(5, Types.DATE);
                } else {
                    userStmt.setDate(5, Date.valueOf(request.ngaySinh()));
                }
                userStmt.setString(6, maKhachHang);
                int userUpdated = userStmt.executeUpdate();

                accountStmt.setString(1, maKhachHang);
                int accountUpdated = accountStmt.executeUpdate();

                customerStmt.setString(1, request.trangThai().trim());
                customerStmt.setString(2, maKhachHang);
                int customerUpdated = customerStmt.executeUpdate();

                if (userUpdated <= 0 || accountUpdated <= 0 || customerUpdated <= 0) {
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
    public boolean softDeleteCustomer(String maKhachHang) throws SQLException {
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (CallableStatement statement = connection.prepareCall("{ call PRC_XOA_KHACH_HANG(?) }")) {
                statement.setString(1, maKhachHang);
                statement.execute();
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
    public boolean restoreCustomer(String maKhachHang) throws SQLException {
        String restoreCustomer = """
                UPDATE KHACH_HANG
                SET TRANGTHAI = 'ACTIVE',
                    IS_DELETED = 0
                WHERE MAKH = ?
                """;
        String restoreUser = """
                UPDATE USERS
                SET IS_DELETED = 0
                WHERE USER_ID = (
                    SELECT USER_ID
                    FROM KHACH_HANG
                    WHERE MAKH = ?
                )
                """;
        String restoreAccount = """
                UPDATE ACCOUNT
                SET STATUS = 'ACTIVE',
                    IS_DELETED = 0
                WHERE USER_ID = (
                    SELECT USER_ID
                    FROM KHACH_HANG
                    WHERE MAKH = ?
                )
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement customerStmt = connection.prepareStatement(restoreCustomer);
                 PreparedStatement userStmt = connection.prepareStatement(restoreUser);
                 PreparedStatement accountStmt = connection.prepareStatement(restoreAccount)) {
                customerStmt.setString(1, maKhachHang);
                int customerUpdated = customerStmt.executeUpdate();
                if (customerUpdated <= 0) {
                    connection.rollback();
                    return false;
                }

                userStmt.setString(1, maKhachHang);
                userStmt.executeUpdate();

                accountStmt.setString(1, maKhachHang);
                accountStmt.executeUpdate();

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

    private static java.time.LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}

