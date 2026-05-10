package com.sportcourt.modules.sport_type.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.sport_type.dto.SportTypeForm;
import com.sportcourt.modules.sport_type.dto.SportTypeSearchCriteria;
import com.sportcourt.modules.sport_type.dto.SportTypeTableRow;
import com.sportcourt.modules.sport_type.entity.SportType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SportTypeDAOImpl implements SportTypeDAO {

    @Override
    public List<SportTypeTableRow> findAll() throws SQLException {
        String sql = """
                SELECT TT.MATT, TT.TEN, TT.DESCRIPTION, TT.CREATED_AT
                FROM LOAI_THE_THAO TT
                WHERE TT.IS_DELETED = 0
                ORDER BY TT.MATT
                """;
        List<SportTypeTableRow> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
                    SportTypeTableRow row = new SportTypeTableRow(rs.getString("MATT"),
                            rs.getString("TEN"),
                            rs.getString("DESCRIPTION"),
                            createdAt == null ? null : createdAt.toLocalDateTime());
                    list.add(row);
                }
                return list;
            }
        }
    }

    @Override
    public List<SportTypeTableRow> findById(String sportId) throws SQLException {
        String sql = """
                SELECT LTT.MATT, LTT.TEN, LTT.DESCRIPTION, LTT.CREATED_AT
                FROM LOAI_THE_THAO LTT
                WHERE LTT.MATT = ?
                AND LTT.IS_DELETED = 0
                """;
        List<SportTypeTableRow> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
                    SportTypeTableRow row = new SportTypeTableRow(rs.getString("MATT"),
                            rs.getString("TEN"),
                            rs.getString("DESCRIPTION"),
                            createdAt == null ? null : createdAt.toLocalDateTime());
                    list.add(row);
                }
                return list;
            }
        }
    }

    @Override
    public List<SportTypeTableRow> search(SportTypeSearchCriteria criteria) throws SQLException {
        String sql = """
                SELECT TT.MATT, TT.TEN, TT.DESCRIPTION, TT.CREATED_AT
                FROM LOAI_THE_THAO TT
                WHERE TT.IS_DELETED = 0
                AND (
                    UPPER(TT.MATT) LIKE ?
                    OR UPPER(TT.TEN) LIKE ?
                    OR UPPER(TT.DESCRIPTION) LIKE ?
                )
                """;
        String keyword = "%" + criteria.getKeyword() + "%";
        List<SportTypeTableRow> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 1; i < 4; i++) {
                ps.setString(i, keyword);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");
                    SportTypeTableRow row = new SportTypeTableRow(rs.getString("MATT"),
                            rs.getString("TEN"),
                            rs.getString("DESCRIPTION"),
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
                SELECT NVL(MAX(TO_NUMBER(SUBSTR(MATT,5))),0) + 1
                FROM LOAI_THE_THAO
                WHERE REGEXP_LIKE(MATT, '^LTT-[0-9]+$')
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "LTT-" + rs.getInt(1);
                }
                return "LTT-1";
            }
        }
    }

    @Override
    public void insert(SportTypeForm sportTypeForm) throws SQLException {
        String sql = """
                INSERT INTO LOAI_THE_THAO
                (MATT, TEN, DESCRIPTION, CREATED_AT, IS_DELETED)
                VALUES
                (?,?,?,SYSDATE,0)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sportTypeForm.getSportId());
            ps.setString(2, sportTypeForm.getName());
            ps.setString(3, sportTypeForm.getDescription());

            ps.executeUpdate();
        }
    }

    @Override
    public void update(SportTypeForm sportTypeForm) throws SQLException{
        String sql = """
                UPDATE LOAI_THE_THAO
                SET TEN = ?,
                    DESCRIPTION = ?
                WHERE MATT = ?
                AND IS_DELETED = 0
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sportTypeForm.getName());
            ps.setString(2, sportTypeForm.getDescription());
            ps.setString(3, sportTypeForm.getSportId());

            ps.executeUpdate();
        }
    }

    @Override
    public void softDelete(SportTypeForm sportTypeForm) throws SQLException{
        String sql = """
                UPDATE LOAI_THE_THAO
                SET IS_DELETED = 1
                WHERE MATT = ?
                AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sportTypeForm.getSportId());

            ps.executeUpdate();
        }
    }

}
