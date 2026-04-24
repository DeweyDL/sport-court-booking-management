package com.sportcourt.managecustomer.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.managecustomer.dto.CustomerProfile;
import com.sportcourt.managecustomer.dto.CustomerSummary;
import com.sportcourt.managecustomer.dto.UpdateCustomerRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcManageCustomerDao implements ManageCustomerDao {
    @Override
    public List<CustomerSummary> findByName(String keyword) throws SQLException {
        String sql = """
                SELECT kh.MAKH, kh.USER_ID, u.HOTEN, u.SDT, kh.TRANG_THAI, kh.DOANH_THU
                FROM KHACH_HANG kh
                JOIN USERS u ON u.USER_ID = kh.USER_ID
                WHERE kh.IS_DELETED = 0
                  AND u.IS_DELETED = 0
                  AND UPPER(u.HOTEN) LIKE '%' || UPPER(?) || '%'
                ORDER BY u.HOTEN ASC
                """;
        List<CustomerSummary> result = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, keyword == null ? "" : keyword.trim());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new CustomerSummary(
                            rs.getString("MAKH"),
                            rs.getString("USER_ID"),
                            rs.getString("HOTEN"),
                            rs.getString("SDT"),
                            rs.getString("TRANG_THAI"),
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
                SELECT kh.MAKH, kh.USER_ID, a.ACCOUNT_ID, u.HOTEN, u.SDT, u.EMAIL, a.USERNAME,
                       kh.TRANG_THAI, kh.MA_HANG, kh.DOANH_THU
                FROM KHACH_HANG kh
                JOIN USERS u ON u.USER_ID = kh.USER_ID
                LEFT JOIN ACCOUNT a ON a.USER_ID = u.USER_ID AND a.IS_DELETED = 0
                WHERE kh.MAKH = ?
                  AND kh.IS_DELETED = 0
                  AND u.IS_DELETED = 0
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
                        rs.getString("EMAIL"),
                        rs.getString("USERNAME"),
                        rs.getString("TRANG_THAI"),
                        rs.getString("MA_HANG"),
                        rs.getBigDecimal("DOANH_THU")
                ));
            }
        }
    }

    @Override
    public void createCustomer(String userId, String accountId, String maKhachHang, CreateCustomerRequest request,
                               String generatedEmail, String passwordHash, String username) throws SQLException {
        String insertUser = """
                INSERT INTO USERS(USER_ID, HOTEN, SDT, EMAIL, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertAccount = """
                INSERT INTO ACCOUNT(ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, 'ACTIVE', SYSDATE, 0)
                """;
        String insertCustomer = """
                INSERT INTO KHACH_HANG(MAKH, USER_ID, MA_HANG, TRANG_THAI, DOANH_THU, CREATED_AT, IS_DELETED)
                VALUES (?, ?, NULL, 'ACTIVE', 0, SYSDATE, 0)
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStmt = connection.prepareStatement(insertUser);
                 PreparedStatement accountStmt = connection.prepareStatement(insertAccount);
                 PreparedStatement customerStmt = connection.prepareStatement(insertCustomer)) {
                userStmt.setString(1, userId);
                userStmt.setString(2, request.hoTen().trim());
                userStmt.setString(3, request.sdt().trim());
                userStmt.setString(4, generatedEmail);
                userStmt.executeUpdate();

                accountStmt.setString(1, accountId);
                accountStmt.setString(2, userId);
                accountStmt.setString(3, username);
                accountStmt.setString(4, passwordHash);
                accountStmt.executeUpdate();

                customerStmt.setString(1, maKhachHang);
                customerStmt.setString(2, userId);
                customerStmt.executeUpdate();

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
                SET u.HOTEN = ?, u.SDT = ?
                WHERE u.USER_ID = (
                    SELECT kh.USER_ID
                    FROM KHACH_HANG kh
                    WHERE kh.MAKH = ?
                      AND kh.IS_DELETED = 0
                )
                  AND u.IS_DELETED = 0
                """;
        String updateCustomer = """
                UPDATE KHACH_HANG
                SET TRANG_THAI = ?
                WHERE MAKH = ?
                  AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement userStmt = connection.prepareStatement(updateUser);
                 PreparedStatement customerStmt = connection.prepareStatement(updateCustomer)) {
                userStmt.setString(1, request.hoTen().trim());
                userStmt.setString(2, request.sdt().trim());
                userStmt.setString(3, maKhachHang);
                int userUpdated = userStmt.executeUpdate();

                customerStmt.setString(1, request.trangThai().trim());
                customerStmt.setString(2, maKhachHang);
                int customerUpdated = customerStmt.executeUpdate();

                if (userUpdated <= 0 || customerUpdated <= 0) {
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
        String deleteCustomer = """
                UPDATE KHACH_HANG
                SET IS_DELETED = 1
                WHERE MAKH = ?
                  AND IS_DELETED = 0
                """;
        String deleteUser = """
                UPDATE USERS
                SET IS_DELETED = 1
                WHERE USER_ID = (
                    SELECT USER_ID
                    FROM KHACH_HANG
                    WHERE MAKH = ?
                )
                  AND IS_DELETED = 0
                """;
        String deleteAccount = """
                UPDATE ACCOUNT
                SET IS_DELETED = 1
                WHERE USER_ID = (
                    SELECT USER_ID
                    FROM KHACH_HANG
                    WHERE MAKH = ?
                )
                  AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement customerStmt = connection.prepareStatement(deleteCustomer);
                 PreparedStatement accountStmt = connection.prepareStatement(deleteAccount);
                 PreparedStatement userStmt = connection.prepareStatement(deleteUser)) {
                customerStmt.setString(1, maKhachHang);
                int customerUpdated = customerStmt.executeUpdate();
                if (customerUpdated <= 0) {
                    connection.rollback();
                    return false;
                }

                accountStmt.setString(1, maKhachHang);
                accountStmt.executeUpdate();

                userStmt.setString(1, maKhachHang);
                userStmt.executeUpdate();

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
}
