package com.sportcourt.modules.customer.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;

import java.sql.Connection;
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
                INSERT INTO KHACH_HANG(MAKH, USER_ID, MA_HANG, TRANGTHAI, DOANH_THU, CREATED_AT, IS_DELETED)
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
                if (generatedEmail == null || generatedEmail.trim().isEmpty()) {
                    userStmt.setNull(4, Types.VARCHAR);
                } else {
                    userStmt.setString(4, generatedEmail);
                }
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
        String deactivateCustomer = """
                UPDATE KHACH_HANG
                SET TRANGTHAI = 'INACTIVE',
                    IS_DELETED = 1
                WHERE MAKH = ?
                """;
        String deactivateUser = """
                UPDATE USERS
                SET IS_DELETED = 1
                WHERE USER_ID = (
                    SELECT USER_ID
                    FROM KHACH_HANG
                    WHERE MAKH = ?
                )
                """;
        String deactivateAccount = """
                UPDATE ACCOUNT
                SET STATUS = 'INACTIVE',
                    IS_DELETED = 1
                WHERE USER_ID = (
                    SELECT USER_ID
                    FROM KHACH_HANG
                    WHERE MAKH = ?
                )
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement customerStmt = connection.prepareStatement(deactivateCustomer);
                 PreparedStatement userStmt = connection.prepareStatement(deactivateUser);
                 PreparedStatement accountStmt = connection.prepareStatement(deactivateAccount)) {
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

