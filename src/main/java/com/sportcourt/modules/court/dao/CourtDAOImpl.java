package com.sportcourt.modules.court.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class CourtDAOImpl implements CourtDAO {
    @Override
    public List<CourtTableRow> findByCriteria(CourtSearchCriteria criteria) throws SQLException {
        String sql = """
                SELECT  sc.MASAN,
                        sc.MAKV,
                        cn.MACN,
                        cn.TEN_CHI_NHANH,
                        ltt.TEN AS TEN_THE_THAO,
                        sc.TRANGTHAI,
                        sc.CREATED_AT
                        FROM SAN_CON sc
                        JOIN KHU_VUC kv
                            ON kv.MAKV = sc.MAKV
                            AND kv.IS_DELETED = 0
                        JOIN LOAI_THE_THAO ltt
                            ON ltt.MATT = kv.MATT
                            AND ltt.IS_DELETED = 0
                        JOIN CHI_NHANH cn
                            ON cn.MACN = kv.MACN
                            AND cn.IS_DELETED = 0
                        WHERE sc.IS_DELETED = 0
                        AND kv.MACN = ?
                """;
        List<Object> params = new ArrayList<>();
        params.add(criteria.getBranchId());
        if (criteria.getKeyWord() != null && !criteria.getKeyWord().isBlank()) {
            sql += ("""
                    AND (
                                      UPPER(sc.MASAN) LIKE UPPER(?)
                                      OR UPPER(sc.MAKV) LIKE UPPER(?)
                                      OR UPPER(ltt.TEN) LIKE UPPER(?)
                                      OR UPPER(cn.TEN_CHI_NHANH) LIKE UPPER(?)
                         )
                    """);
            String keyword = "%" + criteria.getKeyWord().trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (criteria.getAreaId() != null && !criteria.getAreaId().isBlank()) {
            sql += "AND kv.MAKV = ? ";
            params.add(criteria.getAreaId());
        }

        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            sql += "AND sc.TRANGTHAI = ? ";
            params.add(criteria.getStatus());
        }

        sql += "ORDER BY ";
        sql += resolveSortColumn(criteria.getSortBy());
        sql += (" ");
        sql += resolveSortDirection(criteria.getSortDirection());

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<CourtTableRow> rows = new ArrayList<>();
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CREATED_AT");

                    CourtTableRow row = new CourtTableRow(
                            rs.getString("MASAN"),
                            rs.getString("MAKV"),
                            rs.getString("TEN_THE_THAO"),
                            rs.getString("MACN"),
                            rs.getString("TEN_CHI_NHANH"),
                            rs.getString("TRANGTHAI"),
                            createdAt == null ? null : createdAt.toLocalDateTime()
                    );
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    @Override
    public Optional<CourtTableRow> findDetail(String courtId, String branchId) throws SQLException {
        String sql = """
            SELECT  sc.MASAN,
                    sc.MAKV,
                    cn.MACN,
                    cn.TEN_CHI_NHANH,
                    ltt.TEN AS TEN_THE_THAO,
                    sc.TRANGTHAI,
                    sc.CREATED_AT
            FROM SAN_CON sc
            JOIN KHU_VUC kv
                ON kv.MAKV = sc.MAKV
                AND kv.IS_DELETED = 0
            JOIN LOAI_THE_THAO ltt
                ON ltt.MATT = kv.MATT
                AND ltt.IS_DELETED = 0
            JOIN CHI_NHANH cn
                ON cn.MACN = kv.MACN
                AND cn.IS_DELETED = 0
            WHERE sc.IS_DELETED = 0
              AND sc.MASAN = ?
              AND kv.MACN = ?
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, courtId);
            ps.setString(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Timestamp createdAt = rs.getTimestamp("CREATED_AT");

                CourtTableRow row = new CourtTableRow(
                        rs.getString("MASAN"),
                        rs.getString("MAKV"),
                        rs.getString("TEN_THE_THAO"),
                        rs.getString("MACN"),
                        rs.getString("TEN_CHI_NHANH"),
                        rs.getString("TRANGTHAI"),
                        createdAt == null ? null : createdAt.toLocalDateTime()
                );

                return Optional.of(row);
            }
        }
    }

    @Override
    public Optional<Court> findByIdInBranch(String courtId, String branchId) throws SQLException {
        String sql = """
            SELECT sc.MASAN,
                   sc.MAKV,
                   sc.TRANGTHAI,
                   sc.CREATED_AT,
                   sc.IS_DELETED
            FROM SAN_CON sc
            JOIN KHU_VUC kv
                ON kv.MAKV = sc.MAKV
                AND kv.IS_DELETED = 0
            WHERE sc.MASAN = ?
              AND kv.MACN = ?
              AND sc.IS_DELETED = 0
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, courtId);
            ps.setString(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Timestamp createdAt = rs.getTimestamp("CREATED_AT");

                Court court = new Court();
                court.setCourtId(rs.getString("MASAN"));
                court.setAreaId(rs.getString("MAKV"));
                court.setStatus(rs.getString("TRANGTHAI"));
                court.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
                court.setIsDeleted(rs.getInt("IS_DELETED") == 1);

                return Optional.of(court);
            }
        }
    }

    @Override
    public boolean existsById(String courtId) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM SAN_CON
            WHERE MASAN = ?
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, courtId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean areaBelongsToBranch(String areaId, String branchId) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM KHU_VUC
            WHERE MAKV = ?
              AND MACN = ?
              AND IS_DELETED = 0
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, areaId);
            ps.setString(2, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean hasActiveRental(String courtId, String branchId) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM CHI_TIET_HOA_DON_THUE_SAN ct
            JOIN SAN_CON sc
                ON sc.MASAN = ct.MASAN
                AND sc.IS_DELETED = 0
            JOIN KHU_VUC kv
                ON kv.MAKV = sc.MAKV
                AND kv.IS_DELETED = 0
            WHERE ct.MASAN = ?
              AND kv.MACN = ?
              AND ct.IS_DELETED = 0
              AND ct.TRANGTHAI IN (?, ?, ?, ?)
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, courtId);
            ps.setString(2, branchId);
            ps.setString(3, "ĐÃ ĐẶT CHỜ CỌC");
            ps.setString(4, "ĐÃ CỌC");
            ps.setString(5, "ĐÃ XÁC NHẬN");
            ps.setString(6, "ĐANG SỬ DỤNG");

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void insert(Court court) throws SQLException {
        String sql = """
            INSERT INTO SAN_CON (
                MASAN,
                MAKV,
                TRANGTHAI
            ) VALUES (
                ?, ?, ?
            )
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, court.getCourtId());
            ps.setString(2, court.getAreaId());
            ps.setString(3, court.getStatus());

            ps.executeUpdate();
        }
    }

    @Override
    public boolean update(Court court, String branchId) throws SQLException {
        String sql = """
            UPDATE SAN_CON sc
            SET sc.MAKV = ?,
                sc.TRANGTHAI = ?
            WHERE sc.MASAN = ?
              AND sc.IS_DELETED = 0
              AND EXISTS (
                  SELECT 1
                  FROM KHU_VUC old_kv
                  WHERE old_kv.MAKV = sc.MAKV
                    AND old_kv.MACN = ?
                    AND old_kv.IS_DELETED = 0
              )
              AND EXISTS (
                  SELECT 1
                  FROM KHU_VUC new_kv
                  WHERE new_kv.MAKV = ?
                    AND new_kv.MACN = ?
                    AND new_kv.IS_DELETED = 0
              )
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, court.getAreaId());
            ps.setString(2, court.getStatus());
            ps.setString(3, court.getCourtId());
            ps.setString(4, branchId);
            ps.setString(5, court.getAreaId());
            ps.setString(6, branchId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDelete(String courtId, String branchId) throws SQLException {
        String sql = """
            UPDATE SAN_CON sc
            SET sc.IS_DELETED = 1
            WHERE sc.MASAN = ?
              AND sc.IS_DELETED = 0
              AND EXISTS (
                  SELECT 1
                  FROM KHU_VUC kv
                  WHERE kv.MAKV = sc.MAKV
                    AND kv.MACN = ?
                    AND kv.IS_DELETED = 0
              )
            """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, courtId);
            ps.setString(2, branchId);

            return ps.executeUpdate() > 0;
        }
    }

    private String resolveSortColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "sc.MASAN";
        }

        return switch (sortBy) {
            case "courtId" -> "sc.MASAN";
            case "areaId" -> "sc.MAKV";
            case "sportTypeName" -> "ltt.TEN";
            case "status" -> "sc.TRANGTHAI";
            case "createdAt" -> "sc.CREATED_AT";
            default -> "sc.MASAN";
        };
    }

    private String resolveSortDirection(String sortDirection) {
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            return "DESC";
        }

        return "ASC";
    }
}


