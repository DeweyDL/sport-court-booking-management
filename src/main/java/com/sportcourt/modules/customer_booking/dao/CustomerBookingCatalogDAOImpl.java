package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSortBy;
import com.sportcourt.modules.customer_booking.dto.CourtSearchCriteria;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SportTypeOption;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerBookingCatalogDAOImpl implements CustomerBookingCatalogDAO {
    @Override
    public List<BranchOption> findAvailableBranches() throws SQLException {
        String sql = """
                SELECT MACN, TEN_CHI_NHANH, DIACHI
                FROM CHI_NHANH
                WHERE IS_DELETED = 0
                ORDER BY TEN_CHI_NHANH, MACN
                """;
        List<BranchOption> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BranchOption row = new BranchOption(rs.getString("MACN"),
                            rs.getString("TEN_CHI_NHANH"),
                            rs.getString("DIACHI"));
                    list.add(row);
                }
            }
            return list;
        }
    }

    @Override
    public List<SportTypeOption> findAvailableSportTypes(String branchId) throws SQLException {
        String sql = """
                SELECT DISTINCT LTT.MATT, LTT.TEN
                FROM LOAI_THE_THAO LTT
                JOIN KHU_VUC KV
                    ON LTT.MATT = KV.MATT
                    AND KV.IS_DELETED = 0
                JOIN CHI_NHANH CN
                    ON KV.MACN = CN.MACN
                    AND CN.IS_DELETED = 0
                WHERE LTT.IS_DELETED = 0
                    AND KV.MACN = ?
                ORDER BY LTT.TEN, LTT.MATT
                """;
        List<SportTypeOption> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SportTypeOption row = new SportTypeOption(rs.getString("MATT"),
                                                              rs.getString("TEN"));
                    list.add(row);
                }
            }
            return list;
        }
    }

    @Override
    public List<CourtSearchResult> searchCourts(CourtSearchCriteria criteria) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    SC.MASAN AS COURT_ID,
                    KV.MAKV AS AREA_ID,
                    CN.DIACHI AS BRANCH_ADDRESS,
                    LTT.MATT AS SPORT_TYPE_ID,
                    LTT.TEN AS SPORT_TYPE_NAME,
                    MIN(BG.GIA) AS MIN_PRICE,
                    SC.TRANGTHAI AS COURT_STATUS
                FROM SAN_CON SC
                JOIN KHU_VUC KV
                    ON SC.MAKV = KV.MAKV
                    AND KV.IS_DELETED = 0
                JOIN LOAI_THE_THAO LTT
                    ON KV.MATT = LTT.MATT
                    AND LTT.IS_DELETED = 0
                JOIN CHI_NHANH CN
                    ON KV.MACN = CN.MACN
                    AND CN.IS_DELETED = 0
                LEFT JOIN BANG_GIA BG
                    ON BG.MAKV = KV.MAKV
                    AND BG.IS_DELETED = 0
                WHERE SC.IS_DELETED = 0
                """);
        List<Object> params = new ArrayList<>();

        if (criteria != null && !isBlank(criteria.branchId())) {
            sql.append("AND KV.MACN = ? ");
            params.add(criteria.branchId().trim());
        }

        if (criteria != null && !isBlank(criteria.sportTypeId())) {
            sql.append("AND LTT.MATT = ? ");
            params.add(criteria.sportTypeId().trim());
        }

        String keyword = criteria == null ? null : normalizeKeyword(criteria.keyword());
        if (keyword != null) {
            sql.append("""
                    AND (
                        UPPER(SC.MASAN) LIKE ?
                        OR UPPER(LTT.TEN) LIKE ?
                        OR UPPER(CN.TEN_CHI_NHANH) LIKE ?
                        OR UPPER(CN.DIACHI) LIKE ?
                    )
                    """);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        sql.append("""
                GROUP BY SC.MASAN, KV.MAKV, CN.TEN_CHI_NHANH, CN.DIACHI, LTT.MATT, LTT.TEN, SC.TRANGTHAI
                ORDER BY 
                """);
        String sortColumn = resolveSortColumn(criteria == null ? null : criteria.sortBy());
        sql.append(sortColumn);
        sql.append(" ");
        sql.append(resolveSortDirection(criteria == null ? null : criteria.sortDirection()));
        if (!"SC.MASAN".equals(sortColumn)) {
            sql.append(", SC.MASAN");
        }

        List<CourtSearchResult> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourtSearchResult(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Optional<CourtSearchResult> findCourtDetail(String courtId) throws SQLException {
        String sql = """
                SELECT
                    SC.MASAN AS COURT_ID,
                    KV.MAKV AS AREA_ID,
                    CN.DIACHI AS BRANCH_ADDRESS,
                    LTT.MATT AS SPORT_TYPE_ID,
                    LTT.TEN AS SPORT_TYPE_NAME,
                    MIN(BG.GIA) AS MIN_PRICE,
                    SC.TRANGTHAI AS COURT_STATUS
                FROM SAN_CON SC
                JOIN KHU_VUC KV
                    ON SC.MAKV = KV.MAKV
                    AND KV.IS_DELETED = 0
                JOIN LOAI_THE_THAO LTT
                    ON KV.MATT = LTT.MATT
                    AND LTT.IS_DELETED = 0
                JOIN CHI_NHANH CN
                    ON KV.MACN = CN.MACN
                    AND CN.IS_DELETED = 0
                LEFT JOIN BANG_GIA BG
                    ON BG.MAKV = KV.MAKV
                    AND BG.IS_DELETED = 0
                WHERE SC.IS_DELETED = 0
                    AND SC.MASAN = ?
                GROUP BY SC.MASAN, KV.MAKV, CN.DIACHI, LTT.MATT, LTT.TEN, SC.TRANGTHAI
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, courtId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapCourtSearchResult(rs));
            }
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : "%" + trimmed.toUpperCase() + "%";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String resolveSortColumn(CourtSortBy sortBy) {
        if (sortBy == null) {
            return "MIN(BG.GIA)";
        }

        return switch (sortBy) {
            case PRICE -> "MIN(BG.GIA)";
            case COURT_NAME -> "SC.MASAN";
            case BRANCH_NAME -> "CN.TEN_CHI_NHANH";
        };
    }

    private String resolveSortDirection(String sortDirection) {
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            return "DESC";
        }

        return "ASC";
    }

    private CourtSearchResult mapCourtSearchResult(ResultSet rs) throws SQLException {
        return new CourtSearchResult(
                rs.getString("COURT_ID"),
                rs.getString("AREA_ID"),
                rs.getString("BRANCH_ADDRESS"),
                rs.getString("SPORT_TYPE_ID"),
                rs.getString("SPORT_TYPE_NAME"),
                rs.getBigDecimal("MIN_PRICE"),
                rs.getString("COURT_STATUS")
        );
    }
}
