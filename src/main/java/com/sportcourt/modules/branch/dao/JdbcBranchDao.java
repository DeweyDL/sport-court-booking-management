package com.sportcourt.modules.branch.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.branch.dto.BranchCreateRequest;
import com.sportcourt.modules.branch.dto.BranchUpdateRequest;
import com.sportcourt.modules.branch.entity.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBranchDao implements BranchDao {

    @Override
    public List<Branch> findByKeyword(String keyword) throws SQLException {
        String normalizedKeyword = normalizeKeyword(keyword);
        String sql = """
                SELECT MACN, TEN_CHI_NHANH, DIACHI, HOTLINE, CREATED_AT, IS_DELETED
                FROM CHI_NHANH
                WHERE IS_DELETED = 0
                  AND (
                        ? IS NULL
                        OR UPPER(MACN) LIKE ?
                        OR UPPER(TEN_CHI_NHANH) LIKE ?
                        OR UPPER(DIACHI) LIKE ?
                        OR UPPER(HOTLINE) LIKE ?
                  )
                ORDER BY CREATED_AT DESC, MACN ASC
                """;

        List<Branch> branches = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedKeyword);
            statement.setString(2, toLikeValue(normalizedKeyword));
            statement.setString(3, toLikeValue(normalizedKeyword));
            statement.setString(4, toLikeValue(normalizedKeyword));
            statement.setString(5, toLikeValue(normalizedKeyword));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    branches.add(mapBranch(resultSet));
                }
            }
        }
        return branches;
    }

    @Override
    public Optional<Branch> findById(String maCn) throws SQLException {
        String sql = """
                SELECT MACN, TEN_CHI_NHANH, DIACHI, HOTLINE, CREATED_AT, IS_DELETED
                FROM CHI_NHANH
                WHERE MACN = ?
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCn);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBranch(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public String generateNextMaCn() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MACN, '\\d+$'))), 0) + 1 AS NEXT_ID
                FROM CHI_NHANH
                WHERE REGEXP_LIKE(MACN, '^CN-\\d+$')
                """;
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return "CN-" + resultSet.getInt("NEXT_ID");
            }
        }
        return "CN-1";
    }

    @Override
    public void createBranch(BranchCreateRequest request) throws SQLException {
        String sql = """
                INSERT INTO CHI_NHANH (MACN, TEN_CHI_NHANH, DIACHI, HOTLINE, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, SYSDATE, 0)
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, request.maCn());
                statement.setString(2, request.tenChiNhanh());
                statement.setString(3, request.diaChi());
                statement.setString(4, request.hotline());
                statement.executeUpdate();
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
    public void saveBranchChanges(BranchUpdateRequest request) throws SQLException {
        String sql = """
                UPDATE CHI_NHANH
                SET TEN_CHI_NHANH = ?, DIACHI = ?, HOTLINE = ?, IS_DELETED = 0
                WHERE MACN = ?
                """;
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, request.tenChiNhanh());
                statement.setString(2, request.diaChi());
                statement.setString(3, request.hotline());
                statement.setString(4, request.maCn());

                if (statement.executeUpdate() == 0) {
                    throw new SQLException("Khong tim thay chi nhanh de cap nhat: " + request.maCn());
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
    public boolean softDeleteById(String maCn) throws SQLException {
        String sql = """
                UPDATE CHI_NHANH
                SET IS_DELETED = 1
                WHERE MACN = ?
                  AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, maCn);
            return statement.executeUpdate() > 0;
        }
    }

    private Branch mapBranch(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("CREATED_AT");
        LocalDateTime createdDateTime = createdAt == null ? null : createdAt.toLocalDateTime();
        return new Branch(
                resultSet.getString("MACN"),
                resultSet.getString("TEN_CHI_NHANH"),
                resultSet.getString("DIACHI"),
                resultSet.getString("HOTLINE"),
                createdDateTime,
                resultSet.getInt("IS_DELETED") == 1
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

