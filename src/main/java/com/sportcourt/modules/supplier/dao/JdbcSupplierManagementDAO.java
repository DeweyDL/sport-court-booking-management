package com.sportcourt.modules.supplier.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.supplier.entity.Supplier;
import com.sportcourt.modules.supplier.dto.SupplierCreateRequest;
import com.sportcourt.modules.supplier.dto.SupplierUpdateRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSupplierManagementDAO implements SupplierManagementDAO {

    @Override
    public List<Supplier> findSuppliers(String keyword) throws SQLException {
        String sql = "SELECT MANCC, TENNCC, SDT, EMAIL, WEBSITE, DIACHI, CREATED_AT, IS_DELETED " +
                "FROM NHA_CUNG_CAP WHERE UPPER(MANCC) LIKE ? OR UPPER(TENNCC) LIKE ? OR UPPER(SDT) LIKE ? " +
                "ORDER BY CREATED_AT DESC";
        List<Supplier> rows = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (keyword == null ? "" : keyword.trim().toUpperCase()) + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Supplier row = new Supplier();
                    row.setMancc(rs.getString("MANCC"));
                    row.setTenncc(rs.getString("TENNCC"));
                    row.setSdt(rs.getString("SDT"));
                    row.setEmail(rs.getString("EMAIL"));
                    row.setWebsite(rs.getString("WEBSITE"));      // thêm WEBSITE
                    row.setDiachi(rs.getString("DIACHI"));
                    Date createdAt = rs.getDate("CREATED_AT");
                    if (createdAt != null) {
                        row.setCreatedAt(createdAt.toLocalDate());
                    }
                    row.setDeleted(rs.getInt("IS_DELETED") == 1);
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    @Override
    public void createSupplier(String mancc, SupplierCreateRequest request) throws SQLException {
        String sql = "INSERT INTO NHA_CUNG_CAP (MANCC, TENNCC, SDT, EMAIL, WEBSITE, DIACHI, CREATED_AT, IS_DELETED) " +
                "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, 0)";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mancc);
            stmt.setString(2, request.getTenncc().trim());
            stmt.setString(3, request.getSdt().trim());
            stmt.setString(4, request.getEmail()   != null ? request.getEmail().trim()   : null);
            stmt.setString(5, request.getWebsite() != null ? request.getWebsite().trim() : null); // thêm WEBSITE
            stmt.setString(6, request.getDiachi().trim());
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean updateSupplier(SupplierUpdateRequest request) throws SQLException {
        String sql = "UPDATE NHA_CUNG_CAP SET TENNCC = ?, SDT = ?, EMAIL = ?, WEBSITE = ?, DIACHI = ? " +
                "WHERE MANCC = ? AND IS_DELETED = 0";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, request.getTenncc().trim());
            stmt.setString(2, request.getSdt().trim());
            stmt.setString(3, request.getEmail()   != null ? request.getEmail().trim()   : null);
            stmt.setString(4, request.getWebsite() != null ? request.getWebsite().trim() : null); // thêm WEBSITE
            stmt.setString(5, request.getDiachi().trim());
            stmt.setString(6, request.getMancc());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDeleteSupplier(String mancc) throws SQLException {
        String sql = "UPDATE NHA_CUNG_CAP SET IS_DELETED = 1 WHERE MANCC = ? AND IS_DELETED = 0";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mancc);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean restoreSupplier(String mancc) throws SQLException {
        String sql = "UPDATE NHA_CUNG_CAP SET IS_DELETED = 0 WHERE MANCC = ? AND IS_DELETED = 1";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mancc);
            return stmt.executeUpdate() > 0;
        }
    }
}