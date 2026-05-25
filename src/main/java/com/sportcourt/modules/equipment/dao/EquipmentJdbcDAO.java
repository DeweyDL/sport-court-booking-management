package com.sportcourt.modules.equipment.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.equipment.dto.EquipmentCreateRequest;
import com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest;
import com.sportcourt.modules.equipment.entity.Equipment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentJdbcDAO implements EquipmentDAO {

    @Override
    public List<Equipment> findEquipments(String keyword) throws SQLException {
        String sql = "SELECT MADC, TENDC, DVT, GIA, SL_TON, CREATED_AT, IS_DELETED " +
                "FROM DUNG_CU_THE_THAO WHERE 1=1";

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql += " AND (UPPER(MADC) LIKE ? OR UPPER(TENDC) LIKE ?)";
        }
        sql += " ORDER BY IS_DELETED ASC, CREATED_AT DESC";

        List<Equipment> rows = new ArrayList<>();
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
    public Equipment getEquipmentById(String maDc) throws SQLException {
        String sql = "SELECT MADC, TENDC, DVT, GIA, SL_TON, CREATED_AT, IS_DELETED " +
                "FROM DUNG_CU_THE_THAO WHERE MADC = ? AND IS_DELETED = 0";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDc);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public String generateNextMaDc() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MADC, '\\d+$'))), 0) + 1 AS NEXT_ID
                FROM DUNG_CU_THE_THAO
                WHERE REGEXP_LIKE(MADC, '^DC[0-9]+$')
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return "DC" + String.format("%03d", rs.getInt("NEXT_ID"));
            }
        }
        return "DC001";
    }

    @Override
    public void createEquipment(EquipmentCreateRequest request) throws SQLException {
        String sql = "INSERT INTO DUNG_CU_THE_THAO (MADC, TENDC, DVT, GIA, SL_TON, CREATED_AT, IS_DELETED) " +
                "VALUES (?, ?, ?, ?, ?, SYSDATE, 0)";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getMaDc().trim());
            statement.setString(2, request.getTenDc().trim());
            statement.setString(3, request.getDvt() != null ? request.getDvt().trim() : "");
            statement.setBigDecimal(4, request.getGia());
            statement.setInt(5, request.getSlTon());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean updateEquipment(EquipmentUpdateRequest request) throws SQLException {
        // SELECT FOR UPDATE trước khi ghi để tránh Lost Update:
        // nếu 2 manager cùng mở form sửa, chỉ một người được lock hàng tại 1 thời điểm.
        String lockSql = "SELECT SL_TON FROM DUNG_CU_THE_THAO " +
                "WHERE MADC = ? AND IS_DELETED = 0 FOR UPDATE";
        String updateSql = "UPDATE DUNG_CU_THE_THAO SET TENDC = ?, DVT = ?, GIA = ? " +
                "WHERE MADC = ? AND IS_DELETED = 0";
        String adjustStockSql = "{call PRC_DIEU_CHINH_TON_DUNG_CU(?, ?)}";
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            connection.setAutoCommit(false);
            try {
                int currentStock;
                try (PreparedStatement lockStmt = connection.prepareStatement(lockSql)) {
                    lockStmt.setString(1, request.getMaDc().trim());
                    try (ResultSet rs = lockStmt.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }
                        currentStock = rs.getInt("SL_TON");
                    }
                }
                int rows;
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setString(1, request.getTenDc().trim());
                    updateStmt.setString(2, request.getDvt() != null ? request.getDvt().trim() : "");
                    updateStmt.setBigDecimal(3, request.getGia());
                    updateStmt.setString(4, request.getMaDc().trim());
                    rows = updateStmt.executeUpdate();
                }
                if (rows == 0) {
                    connection.rollback();
                    return false;
                }
                int stockDelta = request.getSlTon() - currentStock;
                if (stockDelta != 0) {
                    try (CallableStatement adjustStockStmt = connection.prepareCall(adjustStockSql)) {
                        adjustStockStmt.setString(1, request.getMaDc().trim());
                        adjustStockStmt.setInt(2, stockDelta);
                        adjustStockStmt.execute();
                    }
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
    public boolean softDeleteEquipment(String maDc) throws SQLException {
        String sql = "UPDATE DUNG_CU_THE_THAO SET IS_DELETED = 1 WHERE MADC = ? AND IS_DELETED = 0";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDc);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean restoreEquipment(String maDc) throws SQLException {
        String sql = "UPDATE DUNG_CU_THE_THAO SET IS_DELETED = 0 WHERE MADC = ? AND IS_DELETED = 1";
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maDc);
            return statement.executeUpdate() > 0;
        }
    }

    private Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment row = new Equipment();
        row.setMaDc(rs.getString("MADC"));
        row.setTenDc(rs.getString("TENDC"));
        row.setDvt(rs.getString("DVT"));
        row.setGia(rs.getBigDecimal("GIA"));
        row.setSlTon(rs.getInt("SL_TON"));
        row.setDeleted(rs.getInt("IS_DELETED") == 1);

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            row.setCreatedAt(createdAt.toLocalDateTime());
        }
        return row;
    }
}
