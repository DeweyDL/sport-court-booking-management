package com.sportcourt.modules.cost.dao;

import com.sportcourt.modules.cost.entity.Cost;
import com.sportcourt.common.db.OracleConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostJdbcDAO implements CostDAO {

    @Override
    public List<Cost> search(String keyword) {
        List<Cost> costs = new ArrayList<>();
        String sql = "SELECT * FROM BANG_GIA WHERE IS_DELETED = 0";
        if (keyword != null && !keyword.isBlank()) {
            sql += " AND (LOWER(MABG) LIKE ? OR LOWER(MAKV) LIKE ?)";
        }
        
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                costs.add(mapResultSetToCost(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return costs;
    }

    @Override
    public Cost findByMaBg(String maBg) {
        String sql = "SELECT * FROM BANG_GIA WHERE MABG = ? AND IS_DELETED = 0";
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBg);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToCost(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insert(Cost cost) {
        String sql = "INSERT INTO BANG_GIA (MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA, IS_DELETED) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cost.getMaBg());
            ps.setString(2, cost.getMaKv());
            ps.setInt(3, cost.getGioBatDau());
            ps.setInt(4, cost.getGioKetThuc());
            ps.setBigDecimal(5, cost.getGia());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm bảng giá: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Cost cost) {
        String sql = "UPDATE BANG_GIA SET MAKV = ?, GIOBATDAU = ?, GIOKETTHUC = ?, GIA = ? WHERE MABG = ?";
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cost.getMaKv());
            ps.setInt(2, cost.getGioBatDau());
            ps.setInt(3, cost.getGioKetThuc());
            ps.setBigDecimal(4, cost.getGia());
            ps.setString(5, cost.getMaBg());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật bảng giá: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String maBg) {
        String sql = "UPDATE BANG_GIA SET IS_DELETED = 1 WHERE MABG = ?";
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBg);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa bảng giá: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> getAllKhuVuc() {
        Map<String, String> areas = new HashMap<>();
        String sql = "SELECT MAKV FROM KHU_VUC";
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String makv = rs.getString("MAKV");
                areas.put(makv, ""); // Chỉ dùng mã khu vực
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return areas;
    }

    @Override
    public String generateNextMaBg() {
        String sql = "SELECT MAX(TO_NUMBER(SUBSTR(MABG, 3))) AS MAX_ID FROM BANG_GIA WHERE REGEXP_LIKE(MABG, '^BG[0-9]+$')";
        try (Connection conn = OracleConnection.getOracleConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxId = rs.getInt("MAX_ID");
                return String.format("BG%03d", maxId + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "BG001";
    }

    private Cost mapResultSetToCost(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("CREATED_AT");
        return new Cost(
                rs.getString("MABG"),
                rs.getString("MAKV"),
                rs.getInt("GIOBATDAU"),
                rs.getInt("GIOKETTHUC"),
                rs.getBigDecimal("GIA"),
                rs.getInt("IS_DELETED") == 1,
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
