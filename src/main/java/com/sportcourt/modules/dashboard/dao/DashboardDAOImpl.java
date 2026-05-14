package com.sportcourt.modules.dashboard.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.dashboard.dto.DashboardCourtCard;
import com.sportcourt.modules.dashboard.dto.DashboardSportTypeOption;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAOImpl implements DashboardDAO {

    @Override
    public List<DashboardCourtCard> searchCourts(String keyword) throws SQLException {
        String sql = """
                SELECT
                    sc.MASAN AS COURT_NAME,
                    cn.DIACHI AS BRANCH_ADDRESS,
                    MIN(bg.GIA) AS PRICE
                FROM SAN_CON sc
                JOIN KHU_VUC kv
                    ON kv.MAKV = sc.MAKV
                    AND kv.IS_DELETED = 0
                JOIN CHI_NHANH cn
                    ON cn.MACN = kv.MACN
                    AND cn.IS_DELETED = 0
                LEFT JOIN BANG_GIA bg
                    ON bg.MAKV = kv.MAKV
                    AND bg.IS_DELETED = 0
                WHERE sc.IS_DELETED = 0
                  AND (
                        ? IS NULL
                        OR UPPER(sc.MASAN) LIKE ?
                        OR UPPER(cn.DIACHI) LIKE ?
                        OR UPPER(cn.TEN_CHI_NHANH) LIKE ?
                  )
                GROUP BY sc.MASAN, cn.DIACHI
                ORDER BY sc.MASAN
                """;

        String normalized = normalizeKeyword(keyword);
        List<DashboardCourtCard> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            String likeValue = toLikeValue(normalized);
            ps.setString(2, likeValue);
            ps.setString(3, likeValue);
            ps.setString(4, likeValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal price = rs.getBigDecimal("PRICE");
                    list.add(new DashboardCourtCard(
                            rs.getString("COURT_NAME"),
                            rs.getString("BRANCH_ADDRESS"),
                            price
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<DashboardCourtCard> findCourtsBySportType(String sportTypeId) throws SQLException {
        String sql = """
                SELECT
                    sc.MASAN AS COURT_NAME,
                    cn.DIACHI AS BRANCH_ADDRESS,
                    MIN(bg.GIA) AS PRICE
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
                LEFT JOIN BANG_GIA bg
                    ON bg.MAKV = kv.MAKV
                    AND bg.IS_DELETED = 0
                WHERE sc.IS_DELETED = 0
                  AND ltt.MATT = ?
                GROUP BY sc.MASAN, cn.DIACHI
                ORDER BY sc.MASAN
                """;

        List<DashboardCourtCard> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sportTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal price = rs.getBigDecimal("PRICE");
                    list.add(new DashboardCourtCard(
                            rs.getString("COURT_NAME"),
                            rs.getString("BRANCH_ADDRESS"),
                            price
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<DashboardCourtCard> findCourtsBySportTypeName(String sportTypeName) throws SQLException {
        String sql = """
                SELECT
                    sc.MASAN AS COURT_NAME,
                    cn.DIACHI AS BRANCH_ADDRESS,
                    MIN(bg.GIA) AS PRICE
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
                LEFT JOIN BANG_GIA bg
                    ON bg.MAKV = kv.MAKV
                    AND bg.IS_DELETED = 0
                WHERE sc.IS_DELETED = 0
                  AND UPPER(ltt.TEN) = UPPER(?)
                GROUP BY sc.MASAN, cn.DIACHI
                ORDER BY sc.MASAN
                """;

        List<DashboardCourtCard> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sportTypeName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal price = rs.getBigDecimal("PRICE");
                    list.add(new DashboardCourtCard(
                            rs.getString("COURT_NAME"),
                            rs.getString("BRANCH_ADDRESS"),
                            price
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<DashboardSportTypeOption> findAvailableSportTypes() throws SQLException {
        String sql = """
                SELECT DISTINCT
                    ltt.MATT AS SPORT_TYPE_ID,
                    ltt.TEN  AS SPORT_TYPE_NAME
                FROM LOAI_THE_THAO ltt
                JOIN KHU_VUC kv
                    ON kv.MATT = ltt.MATT
                    AND kv.IS_DELETED = 0
                JOIN SAN_CON sc
                    ON sc.MAKV = kv.MAKV
                    AND sc.IS_DELETED = 0
                WHERE ltt.IS_DELETED = 0
                ORDER BY ltt.TEN
                """;

        List<DashboardSportTypeOption> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DashboardSportTypeOption(
                        rs.getString("SPORT_TYPE_ID"),
                        rs.getString("SPORT_TYPE_NAME")
                ));
            }
        }
        return list;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toLikeValue(String keyword) {
        return keyword == null ? null : "%" + keyword + "%";
    }
}
