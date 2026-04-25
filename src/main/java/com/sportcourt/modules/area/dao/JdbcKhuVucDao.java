package com.sportcourt.modules.area.dao;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.KhuVuc;
import com.sportcourt.modules.area.enitity.KhuVucCreateRequest;
import com.sportcourt.modules.area.enitity.KhuVucUpdateRequest;
import com.sportcourt.modules.area.enitity.LoaiTheThao;
import com.sportcourt.modules.area.enitity.SanConDraft;
import com.sportcourt.modules.area.enitity.SanCon;
import com.sportcourt.modules.area.util.AreaSqlLoader;
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

// JDBC implementation cho module khu vuc. File nay tap trung vao query khu vuc va danh sach san con.
public class JdbcKhuVucDao implements KhuVucDao {
    private static final String SAN_CON_BY_KHU_VUC_SQL = AreaSqlLoader.load("db/SANCONCUAKHUVUC.sql");
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
    public List<KhuVuc> findByKeyword(String keyword) throws SQLException {
        String normalizedKeyword = normalizeKeyword(keyword);
        String sql = """
                SELECT k.MAKV, k.MACN, k.MATT, ltt.TEN AS TEN_THE_THAO, k.SO_LUONG_SAN, k.CREATED_AT
                FROM KHU_VUC k
                JOIN LOAI_THE_THAO ltt ON ltt.MATT = k.MATT AND ltt.IS_DELETED = 0
                WHERE k.IS_DELETED = 0
                  AND (
                        ? IS NULL
                        OR UPPER(k.MAKV) LIKE ?
                        OR UPPER(k.MACN) LIKE ?
                        OR UPPER(k.MATT) LIKE ?
                        OR UPPER(ltt.TEN) LIKE ?
                  )
                ORDER BY k.CREATED_AT DESC, k.MAKV ASC
                """;

        List<KhuVuc> khuVucs = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedKeyword);
            statement.setString(2, toLikeValue(normalizedKeyword));
            statement.setString(3, toLikeValue(normalizedKeyword));
            statement.setString(4, toLikeValue(normalizedKeyword));
            statement.setString(5, toLikeValue(normalizedKeyword));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    khuVucs.add(mapKhuVuc(resultSet));
                }
            }
        }
        return khuVucs;
    }

    @Override
    public Optional<KhuVuc> findById(String maKv) throws SQLException {
        String sql = """
                SELECT k.MAKV, k.MACN, k.MATT, ltt.TEN AS TEN_THE_THAO, k.SO_LUONG_SAN, k.CREATED_AT
                FROM KHU_VUC k
                JOIN LOAI_THE_THAO ltt ON ltt.MATT = k.MATT AND ltt.IS_DELETED = 0
                WHERE k.MAKV = ?
                  AND k.IS_DELETED = 0
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
    public List<ChiNhanh> findChiNhanhList() throws SQLException {
        List<ChiNhanh> chiNhanhs = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(CHI_NHANH_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                chiNhanhs.add(new ChiNhanh(
                        resultSet.getString("MACN"),
                        resultSet.getString("TEN_CHI_NHANH")
                ));
            }
        }
        return chiNhanhs;
    }

    @Override
    public List<LoaiTheThao> findLoaiTheThaoList() throws SQLException {
        List<LoaiTheThao> loaiTheThaos = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(LOAI_THE_THAO_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                loaiTheThaos.add(new LoaiTheThao(
                        resultSet.getString("MATT"),
                        resultSet.getString("TEN")
                ));
            }
        }
        return loaiTheThaos;
    }

    @Override
    public List<SanCon> findSanConByKhuVuc(String maKv) throws SQLException {
        List<SanCon> sanCons = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(SAN_CON_BY_KHU_VUC_SQL)) {
            statement.setString(1, maKv);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sanCons.add(mapSanCon(resultSet));
                }
            }
        }
        return sanCons;
    }

    @Override
    public void createKhuVuc(KhuVucCreateRequest request) throws SQLException {
        String insertKhuVucSql = """
                INSERT INTO KHU_VUC (MAKV, MACN, MATT, SO_LUONG_SAN, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertSanConSql = """
                INSERT INTO SAN_CON (MASAN, MAKV, TRANGTHAI, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, SYSDATE, 0)
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement insertKhuVucStatement = connection.prepareStatement(insertKhuVucSql);
                 PreparedStatement insertSanConStatement = connection.prepareStatement(insertSanConSql)) {
                insertKhuVucStatement.setString(1, request.maKv());
                insertKhuVucStatement.setString(2, request.maCn());
                insertKhuVucStatement.setString(3, request.maTt());
                insertKhuVucStatement.setInt(4, request.soLuongSan());
                insertKhuVucStatement.executeUpdate();

                for (SanConDraft sanConDraft : request.sanCons()) {
                    insertSanConStatement.setString(1, sanConDraft.maSan());
                    insertSanConStatement.setString(2, request.maKv());
                    insertSanConStatement.setString(3, sanConDraft.trangThai());
                    insertSanConStatement.addBatch();
                }
                insertSanConStatement.executeBatch();

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
    public void saveKhuVucChanges(KhuVucUpdateRequest request) throws SQLException {
        String updateKhuVucSql = """
                UPDATE KHU_VUC
                SET MATT = ?,
                    SO_LUONG_SAN = ?
                WHERE MAKV = ?
                  AND IS_DELETED = 0
                """;
        String insertSanConSql = """
                INSERT INTO SAN_CON (MASAN, MAKV, TRANGTHAI, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, SYSDATE, 0)
                """;
        String deleteSanConSql = """
                UPDATE SAN_CON
                SET IS_DELETED = 1
                WHERE MASAN = ?
                  AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement updateKhuVucStatement = connection.prepareStatement(updateKhuVucSql);
                 PreparedStatement insertSanConStatement = connection.prepareStatement(insertSanConSql);
                 PreparedStatement deleteSanConStatement = connection.prepareStatement(deleteSanConSql)) {
                updateKhuVucStatement.setString(1, request.maTt());
                updateKhuVucStatement.setInt(2, request.soLuongSan());
                updateKhuVucStatement.setString(3, request.maKv());

                if (updateKhuVucStatement.executeUpdate() == 0) {
                    throw new SQLException("Khong tim thay khu vuc de cap nhat: " + request.maKv());
                }

                for (SanConDraft sanConDraft : request.newSanCons()) {
                    insertSanConStatement.setString(1, sanConDraft.maSan());
                    insertSanConStatement.setString(2, request.maKv());
                    insertSanConStatement.setString(3, sanConDraft.trangThai());
                    insertSanConStatement.addBatch();
                }
                insertSanConStatement.executeBatch();

                for (String maSan : request.deletedSanConIds()) {
                    deleteSanConStatement.setString(1, maSan);
                    deleteSanConStatement.addBatch();
                }
                deleteSanConStatement.executeBatch();

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

    @Override
    public boolean softDeleteSanConById(String maSan) throws SQLException {
        String sql = """
                UPDATE SAN_CON
                SET IS_DELETED = 1
                WHERE MASAN = ?
                  AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maSan);
            return statement.executeUpdate() > 0;
        }
    }

    // Mapping khu vuc tu ResultSet thanh Java object de view khong phai xu ly JDBC.
    private KhuVuc mapKhuVuc(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("CREATED_AT");
        LocalDateTime createdDateTime = createdAt == null ? null : createdAt.toLocalDateTime();
        return new KhuVuc(
                resultSet.getString("MAKV"),
                resultSet.getString("MACN"),
                resultSet.getString("MATT"),
                resultSet.getString("TEN_THE_THAO"),
                resultSet.getInt("SO_LUONG_SAN"),
                createdDateTime
        );
    }

    // Mapping san con de man chi tiet co the render bang danh sach san con.
    private SanCon mapSanCon(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("CREATED_AT");
        LocalDateTime createdDateTime = createdAt == null ? null : createdAt.toLocalDateTime();
        return new SanCon(
                resultSet.getString("MASAN"),
                resultSet.getString("MAKV"),
                resultSet.getString("TRANGTHAI"),
                createdDateTime
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
