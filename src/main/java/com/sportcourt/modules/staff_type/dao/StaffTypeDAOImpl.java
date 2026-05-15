package com.sportcourt.modules.staff_type.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff_type.dto.StaffTypeForm;
import com.sportcourt.modules.staff_type.dto.StaffTypeSearchCriteria;
import com.sportcourt.modules.staff_type.dto.StaffTypeTableRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffTypeDAOImpl implements StaffTypeDAO {

    @Override
    public List<StaffTypeTableRow> findAll() throws SQLException {
        String sql = """
                SELECT LNV.MALNV, LNV.VITRI, LNV.MUC_LUONG, LNV.CREATED_AT
                FROM LOAI_NHAN_VIEN LNV
                WHERE LNV.IS_DELETED = 0
                ORDER BY LNV.MALNV
                """;
        List<StaffTypeTableRow> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
                    StaffTypeTableRow row = new StaffTypeTableRow(
                            rs.getString("MALNV"),
                            rs.getString("VITRI"),
                            rs.getBigDecimal("MUC_LUONG"),
                            createdAt == null ? null : createdAt.toLocalDateTime());
                    list.add(row);
                }
                return list;
            }
        }
    }

    @Override
    public List<StaffTypeTableRow> findById(String staffTypeId) throws SQLException {
        String sql = """
                SELECT LNV.MALNV, LNV.VITRI, LNV.MUC_LUONG, LNV.CREATED_AT
                FROM LOAI_NHAN_VIEN LNV
                WHERE LNV.MALNV = ?
                AND LNV.IS_DELETED = 0
                """;
        List<StaffTypeTableRow> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, staffTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
                    StaffTypeTableRow row = new StaffTypeTableRow(
                            rs.getString("MALNV"),
                            rs.getString("VITRI"),
                            rs.getBigDecimal("MUC_LUONG"),
                            createdAt == null ? null : createdAt.toLocalDateTime());
                    list.add(row);
                }
                return list;
            }
        }
    }

    @Override
    public List<StaffTypeTableRow> search(StaffTypeSearchCriteria criteria) throws SQLException {
        String sql = """
                SELECT LNV.MALNV, LNV.VITRI, LNV.MUC_LUONG, LNV.CREATED_AT
                FROM LOAI_NHAN_VIEN LNV
                WHERE LNV.IS_DELETED = 0
                AND (
                    UPPER(LNV.MALNV) LIKE ?
                    OR UPPER(LNV.VITRI) LIKE ?
                )
                """;
        String keyword = "%" + criteria.getKeyword().toUpperCase() + "%";
        List<StaffTypeTableRow> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
                    StaffTypeTableRow row = new StaffTypeTableRow(
                            rs.getString("MALNV"),
                            rs.getString("VITRI"),
                            rs.getBigDecimal("MUC_LUONG"),
                            createdAt == null ? null : createdAt.toLocalDateTime());
                    list.add(row);
                }
            }
            return list;
        }
    }

    @Override
    public String generateNextId() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(SUBSTR(MALNV,5))),0) + 1
                FROM LOAI_NHAN_VIEN
                WHERE REGEXP_LIKE(MALNV, '^LNV-[0-9]+$')
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "LNV-" + rs.getInt(1);
                }
                return "LNV-1";
            }
        }
    }

    @Override
    public void insert(StaffTypeForm form) throws SQLException {
        String sql = """
                INSERT INTO LOAI_NHAN_VIEN
                (MALNV, VITRI, MUC_LUONG, CREATED_AT, IS_DELETED)
                VALUES
                (?, ?, ?, SYSDATE, 0)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, form.getStaffTypeId());
            ps.setString(2, form.getPosition());
            ps.setBigDecimal(3, form.getSalary());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(StaffTypeForm form) throws SQLException {
        String sql = """
                UPDATE LOAI_NHAN_VIEN
                SET VITRI = ?,
                    MUC_LUONG = ?
                WHERE MALNV = ?
                AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, form.getPosition());
            ps.setBigDecimal(2, form.getSalary());
            ps.setString(3, form.getStaffTypeId());
            ps.executeUpdate();
        }
    }

    @Override
    public void softDelete(StaffTypeForm form) throws SQLException {
        String sql = """
                UPDATE LOAI_NHAN_VIEN
                SET IS_DELETED = 1
                WHERE MALNV = ?
                AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, form.getStaffTypeId());
            ps.executeUpdate();
        }
    }
}
