package com.sportcourt.modules.area.dao;

import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.dto.AreaCreateRequest;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;
import com.sportcourt.common.db.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAreaDao implements AreaDao {
    private static final String CHI_NHANH_SQL = """
            SELECT MACN, TEN_CHI_NHANH
            FROM CHI_NHANH
            WHERE IS_DELETED = 0
            ORDER BY TEN_CHI_NHANH ASC, MACN ASC
            """;
    private static final String LOAI_THE_THAO_SQL = """
            SELECT MATT, TEN
            FROM LOAI_THE_THAO
            WHERE IS_DELETED = 0
            ORDER BY TEN ASC, MATT ASC
            """;

    @Override
    public List<Area> findByKeyword(String keyword) throws SQLException {
        String normalizedKeyword = normalizeKeyword(keyword);
        String sql = """
                SELECT k.MAKV, k.MACN, k.MATT, ltt.TEN AS TEN_THE_THAO, k.SO_LUONG_SAN, k.CREATED_AT, k.IS_DELETED
                FROM KHU_VUC k
                JOIN LOAI_THE_THAO ltt ON ltt.MATT = k.MATT AND ltt.IS_DELETED = 0
                WHERE (
                        ? IS NULL
                        OR UPPER(k.MAKV) LIKE ?
                        OR UPPER(k.MACN) LIKE ?
                        OR UPPER(k.MATT) LIKE ?
                        OR UPPER(ltt.TEN) LIKE ?
                  )
                ORDER BY k.CREATED_AT DESC, k.MAKV ASC
                """;

        List<Area> areas = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedKeyword);
            statement.setString(2, toLikeValue(normalizedKeyword));
            statement.setString(3, toLikeValue(normalizedKeyword));
            statement.setString(4, toLikeValue(normalizedKeyword));
            statement.setString(5, toLikeValue(normalizedKeyword));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    areas.add(mapKhuVuc(resultSet));
                }
            }
        }
        return areas;
    }

    @Override
    public Optional<Area> findById(String maKv) throws SQLException {
        String sql = """
                SELECT k.MAKV, k.MACN, k.MATT, ltt.TEN AS TEN_THE_THAO, k.SO_LUONG_SAN, k.CREATED_AT, k.IS_DELETED
                FROM KHU_VUC k
                JOIN LOAI_THE_THAO ltt ON ltt.MATT = k.MATT AND ltt.IS_DELETED = 0
                WHERE k.MAKV = ?
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKv);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapKhuVuc(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Area.ChiNhanhOption> findChiNhanhList() throws SQLException {
        List<Area.ChiNhanhOption> chiNhanhs = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(CHI_NHANH_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                chiNhanhs.add(new Area.ChiNhanhOption(
                        resultSet.getString("MACN"),
                        resultSet.getString("TEN_CHI_NHANH")
                ));
            }
        }
        return chiNhanhs;
    }

    @Override
    public List<Area.SportTypeOption> findLoaiTheThaoList() throws SQLException {
        List<Area.SportTypeOption> sportTypes = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(LOAI_THE_THAO_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                sportTypes.add(new Area.SportTypeOption(
                        resultSet.getString("MATT"),
                        resultSet.getString("TEN")
                ));
            }
        }
        return sportTypes;
    }

    @Override
    public String generateNextMaKv() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MAKV, '\\d+$'))), 0) + 1 AS NEXT_ID
                FROM KHU_VUC
                WHERE REGEXP_LIKE(MAKV, '^KV\\d+$')
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return "KV%03d".formatted(resultSet.getInt("NEXT_ID"));
            }
        }
        throw new SQLException("Khong the sinh ma khu vuc moi.");
    }

    @Override
    public String findDefaultChiNhanhId() throws SQLException {
        String sql = """
                SELECT MACN
                FROM CHI_NHANH
                WHERE IS_DELETED = 0
                ORDER BY MACN ASC
                FETCH FIRST 1 ROWS ONLY
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString("MACN");
            }
        }
        throw new SQLException("Khong tim thay chi nhanh nao de tao khu vuc.");
    }

    @Override
    public void createKhuVuc(AreaCreateRequest request) throws SQLException {
        String insertKhuVucSql = """
                INSERT INTO KHU_VUC (MAKV, MACN, MATT, SO_LUONG_SAN, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, SYSDATE, 0)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement insertKhuVucStatement = connection.prepareStatement(insertKhuVucSql)) {
                insertKhuVucStatement.setString(1, request.maKv());
                insertKhuVucStatement.setString(2, request.maCn());
                insertKhuVucStatement.setString(3, request.maTt());
                insertKhuVucStatement.setInt(4, request.soLuongSan());
                insertKhuVucStatement.executeUpdate();

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public void saveKhuVucChanges(AreaUpdateRequest request) throws SQLException {
        String updateKhuVucSql = """
                UPDATE KHU_VUC
                SET MATT = ?, IS_DELETED = 0
                WHERE MAKV = ?
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement updateKhuVucStatement = connection.prepareStatement(updateKhuVucSql)) {
                updateKhuVucStatement.setString(1, request.maTt());
                updateKhuVucStatement.setString(2, request.maKv());

                if (updateKhuVucStatement.executeUpdate() == 0) {
                    throw new SQLException("Khong tim thay khu vuc de cap nhat: " + request.maKv());
                }

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean softDeleteById(String maKv) throws SQLException {
        String sql = """
                UPDATE KHU_VUC
                SET IS_DELETED = 1
                WHERE MAKV = ?
                  AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maKv);
            return statement.executeUpdate() > 0;
        }
    }

    private Area mapKhuVuc(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("CREATED_AT");
        LocalDateTime createdDateTime = createdAt == null ? null : createdAt.toLocalDateTime();
        return new Area(
                resultSet.getString("MAKV"),
                resultSet.getString("MACN"),
                resultSet.getString("MATT"),
                resultSet.getString("TEN_THE_THAO"),
                resultSet.getInt("SO_LUONG_SAN"),
                createdDateTime,
                resultSet.getInt("IS_DELETED") == 1 // Ánh xạ trường IS_DELETED
        );
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
