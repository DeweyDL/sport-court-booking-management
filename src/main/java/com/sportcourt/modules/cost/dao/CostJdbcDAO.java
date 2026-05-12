package com.sportcourt.modules.cost.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.cost.dto.CostCreateRequest;
import com.sportcourt.modules.cost.dto.CostUpdateRequest;
import com.sportcourt.modules.cost.entity.Cost;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CostJdbcDAO implements CostDAO {

    @Override
    public List<Cost> findCosts(String keyword) throws SQLException {
        String sql = "SELECT MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, CREATED_AT " +
                "FROM BANG_GIA WHERE IS_DELETED = 0";

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql += " AND (UPPER(MABG) LIKE ? OR UPPER(MAKV) LIKE ?)";
        }
        sql += " ORDER BY CREATED_AT DESC";

        List<Cost> rows = new ArrayList<>();
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
    public Cost getCostById(String maBg) throws SQLException {
        String sql = "SELECT MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, CREATED_AT " +
                "FROM BANG_GIA WHERE MABG = ? AND IS_DELETED = 0";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maBg);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void createCost(CostCreateRequest request) throws SQLException {
        // Mock ID generation, replace with sequence if available
        String generatedMaBg = "BG" + String.format("%04d", new Random().nextInt(10000));

        String sql = "INSERT INTO BANG_GIA (MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, CREATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, SYSDATE)";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, generatedMaBg);
            statement.setString(2, request.getMaKv() != null ? request.getMaKv().trim() : null);
            statement.setInt(3, request.getGioBatDau());
            statement.setInt(4, request.getGioKetThuc());
            statement.setBigDecimal(5, request.getGia());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean updateCost(CostUpdateRequest request) throws SQLException {
        String sql = "UPDATE BANG_GIA SET MAKV = ?, GIOBATDAU = ?, GIOKETTHUC = ?, GIA = ? " +
                "WHERE MABG = ?";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getMaKv() != null ? request.getMaKv().trim() : null);
            statement.setInt(2, request.getGioBatDau());
            statement.setInt(3, request.getGioKetThuc());
            statement.setBigDecimal(4, request.getGia());
            statement.setString(5, request.getMaBg().trim());
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCost(String maBg) throws SQLException {
        String sql = "UPDATE BANG_GIA SET IS_DELETED = 1 WHERE MABG = ?";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maBg);
            return statement.executeUpdate() > 0;
        }
    }

    private Cost mapRow(ResultSet rs) throws SQLException {
        Cost row = new Cost();
        row.setMaBg(rs.getString("MABG"));
        row.setMaKv(rs.getString("MAKV"));
        row.setGioBatDau(rs.getInt("GIOBATDAU"));
        row.setGioKetThuc(rs.getInt("GIOKETTHUC"));
        row.setGia(rs.getBigDecimal("GIA"));

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            row.setCreatedAt(createdAt.toLocalDateTime());
        }
        return row;
    }

    @Override
    public List<String> getAllKhuVucIds() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MAKV FROM KHU_VUC WHERE IS_DELETED = 0 ORDER BY MAKV";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MAKV"));
            }
        }
        return list;
    }
}
