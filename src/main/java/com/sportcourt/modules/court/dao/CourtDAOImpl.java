import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.court.dao.CourtDAO;
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
    public Optional<Court> findIdByInBranch(String courtId, String branchId) throws SQLException {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String courtId) throws SQLException {
        return false;
    }

    @Override
    public boolean areaBelongsToBranch(String AreaId, String branchId) throws SQLException {
        return false;
    }

    @Override
    public boolean hasActiveRental(String courtId, String branchId) throws SQLException {
        return false;
    }

    @Override
    public void insert(Court court) throws SQLException {

    }

    @Override
    public boolean update(Court court, String branchId) throws SQLException {
        return false;
    }

    @Override
    public boolean softDelete(String courtId, String branchId) throws SQLException {
        return false;
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
