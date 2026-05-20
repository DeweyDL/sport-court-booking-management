package com.sportcourt.modules.customer_rank.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer_rank.dto.CustomerRankCreateRequest;
import com.sportcourt.modules.customer_rank.dto.CustomerRankUpdateRequest;
import com.sportcourt.modules.customer_rank.entity.CustomerRank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerRankJdbcDAO implements CustomerRankDAO {

    @Override
    public List<CustomerRank> findCustomerRanks(String keyword) throws SQLException {
        String sql = "SELECT MA_HANG, TEN_HANG, CHIET_KHAU, MUC_TIEN, CREATED_AT, IS_DELETED " +
                "FROM HANG_KHACH_HANG WHERE IS_DELETED = 0";

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql += " AND (UPPER(MA_HANG) LIKE ? OR UPPER(TEN_HANG) LIKE ?)";
        }
        sql += " ORDER BY CREATED_AT DESC";

        List<CustomerRank> rows = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (hasKeyword) {
                String likeKeyword = "%" + keyword.trim().toUpperCase() + "%";
                statement.setString(1, likeKeyword);
                statement.setString(2, likeKeyword);
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        }
        return rows;
    }

    @Override
    public CustomerRank getCustomerRankById(String maHang) throws SQLException {
        String sql = "SELECT MA_HANG, TEN_HANG, CHIET_KHAU, MUC_TIEN, CREATED_AT, IS_DELETED " +
                "FROM HANG_KHACH_HANG WHERE MA_HANG = ? AND IS_DELETED = 0";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maHang);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void createCustomerRank(CustomerRankCreateRequest request) throws SQLException {
        String sql = "INSERT INTO HANG_KHACH_HANG (MA_HANG, TEN_HANG, CHIET_KHAU, MUC_TIEN, CREATED_AT, IS_DELETED) " +
                "VALUES (?, ?, ?, ?, SYSDATE, 0)";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getMaHang().trim());
            statement.setString(2, request.getTenHang().trim());
            statement.setBigDecimal(3, request.getChietKhau());
            statement.setBigDecimal(4, request.getMucTien());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean updateCustomerRank(CustomerRankUpdateRequest request) throws SQLException {
        String sql = "UPDATE HANG_KHACH_HANG SET TEN_HANG = ?, CHIET_KHAU = ?, MUC_TIEN = ? " +
                "WHERE MA_HANG = ? AND IS_DELETED = 0";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getTenHang().trim());
            statement.setBigDecimal(2, request.getChietKhau());
            statement.setBigDecimal(3, request.getMucTien());
            statement.setString(4, request.getMaHang().trim());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDeleteCustomerRank(String maHang) throws SQLException {
        String sql = "UPDATE HANG_KHACH_HANG SET IS_DELETED = 1 WHERE MA_HANG = ? AND IS_DELETED = 0";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maHang);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public String generateNextMaHang() throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MA_HANG, '\\d+$'))), 0) + 1 AS NEXT_ID " +
                "FROM HANG_KHACH_HANG WHERE REGEXP_LIKE(MA_HANG, '^HKH-\\d+$')";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return "HKH-" + rs.getInt("NEXT_ID");
            }
        }
        return "HKH-1";
    }

    private CustomerRank mapRow(ResultSet rs) throws SQLException {
        CustomerRank row = new CustomerRank();
        row.setMaHang(rs.getString("MA_HANG"));
        row.setTenHang(rs.getString("TEN_HANG"));
        row.setChietKhau(rs.getBigDecimal("CHIET_KHAU"));
        row.setMucTien(rs.getBigDecimal("MUC_TIEN"));
        row.setDeleted(rs.getInt("IS_DELETED") == 1);

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            row.setCreatedAt(createdAt.toLocalDateTime());
        }
        return row;
    }
}
