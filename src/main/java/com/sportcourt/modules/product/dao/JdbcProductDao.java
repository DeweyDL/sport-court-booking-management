package com.sportcourt.modules.product.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.entity.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductDao implements ProductDao {
    private static final String ACTIVE_STATUS = "HOẠT ĐỘNG";
    private static final String OUT_OF_STOCK_STATUS = "HẾT HÀNG";
    private static final String DELETED_STATUS = "ĐÃ XOÁ";

    @Override
    public List<ProductResponse> search(ProductSearchCriteria criteria) throws SQLException {
        ProductSearchCriteria safeCriteria = criteria == null ? new ProductSearchCriteria() : criteria;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MASP, TENSP, DVT, GIA, SL_TON, NVL(IS_DELETED, 0) AS IS_DELETED ");
        sql.append("FROM SAN_PHAM ");
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (!Boolean.TRUE.equals(safeCriteria.getIncludeDeleted())) {
            sql.append("AND NVL(IS_DELETED, 0) = 0 ");
        }

        if (!isBlank(safeCriteria.getKeyword())) {
            String keyword = "%" + safeCriteria.getKeyword().trim() + "%";
            sql.append("AND (LOWER(MASP) LIKE LOWER(?) ");
            sql.append("OR LOWER(TENSP) LIKE LOWER(?) ");
            sql.append("OR LOWER(DVT) LIKE LOWER(?)) ");
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (!isBlank(safeCriteria.getDanhMuc())) {
            sql.append("AND LOWER(DVT) = LOWER(?) ");
            params.add(safeCriteria.getDanhMuc().trim());
        }

        sql.append("ORDER BY NVL(IS_DELETED, 0), CREATED_AT DESC, MASP DESC");

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<ProductResponse> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapResponse(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    @Override
    public Optional<ProductResponse> findById(String maSp) throws SQLException {
        if (isBlank(maSp)) {
            return Optional.empty();
        }

        String sql = ""
                + "SELECT MASP, TENSP, DVT, GIA, SL_TON, NVL(IS_DELETED, 0) AS IS_DELETED "
                + "FROM SAN_PHAM "
                + "WHERE MASP = ?";

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSp.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResponse(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    @Override
    public boolean existsByName(String tenSp, String exceptMaSp) throws SQLException {
        if (isBlank(tenSp)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM SAN_PHAM ");
        sql.append("WHERE LOWER(TENSP) = LOWER(?) ");
        sql.append("AND NVL(IS_DELETED, 0) = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(tenSp.trim());

        if (!isBlank(exceptMaSp)) {
            sql.append("AND MASP <> ? ");
            params.add(exceptMaSp.trim());
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            return count(conn, sql.toString(), params) > 0;
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    @Override
    public void insert(Connection conn, Product product) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        String sql = ""
                + "INSERT INTO SAN_PHAM "
                + "(MASP, TENSP, DVT, GIA, SL_TON, CREATED_AT, IS_DELETED) "
                + "VALUES (?, ?, ?, ?, ?, SYSDATE, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nextProductId(conn));
            ps.setString(2, product.getTenSp());
            ps.setString(3, product.getDanhMuc());
            ps.setBigDecimal(4, product.getGia());
            ps.setInt(5, product.getSoLuongTon() == null ? 0 : product.getSoLuongTon());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    @Override
    public boolean update(Connection conn, Product product) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        String sql = ""
                + "UPDATE SAN_PHAM "
                + "SET TENSP = ?, DVT = ?, GIA = ?, SL_TON = ? "
                + "WHERE MASP = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getTenSp());
            ps.setString(2, product.getDanhMuc());
            ps.setBigDecimal(3, product.getGia());
            ps.setInt(4, product.getSoLuongTon() == null ? 0 : product.getSoLuongTon());
            ps.setString(5, product.getMaSp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    @Override
    public boolean softDelete(Connection conn, String maSp) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        String sql = ""
                + "UPDATE SAN_PHAM "
                + "SET IS_DELETED = 1 "
                + "WHERE MASP = ? "
                + "AND NVL(IS_DELETED, 0) = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSp);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    @Override
    public boolean restore(Connection conn, String maSp) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        String sql = ""
                + "UPDATE SAN_PHAM "
                + "SET IS_DELETED = 0 "
                + "WHERE MASP = ? "
                + "AND NVL(IS_DELETED, 0) = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSp);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw enrichSchemaError(e);
        }
    }

    private ProductResponse mapResponse(ResultSet rs) throws SQLException {
        ProductResponse response = new ProductResponse();

        response.setMaSp(rs.getString("MASP"));
        response.setTenSp(rs.getString("TENSP"));
        response.setDanhMuc(rs.getString("DVT"));

        BigDecimal gia = rs.getBigDecimal("GIA");
        response.setGia(gia == null ? BigDecimal.ZERO : gia);

        int soLuong = rs.getInt("SL_TON");
        if (rs.wasNull()) {
            soLuong = 0;
        }
        response.setSoLuongTon(soLuong);

        boolean deleted = rs.getInt("IS_DELETED") == 1;
        response.setDeleted(deleted);

        if (deleted) {
            response.setTrangThai(DELETED_STATUS);
        } else if (soLuong <= 0) {
            response.setTrangThai(OUT_OF_STOCK_STATUS);
        } else {
            response.setTrangThai(ACTIVE_STATUS);
        }

        return response;
    }

    private String nextProductId(Connection conn) throws SQLException {
        String sql = ""
                + "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MASP, '[0-9]+$'))), 0) + 1 AS NEXT_NUM "
                + "FROM SAN_PHAM "
                + "WHERE REGEXP_LIKE(MASP, '^SP[0-9]+$')";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt("NEXT_NUM");
            }
            return String.format("SP%02d", next);
        }
    }

    private int count(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int index = 0; index < params.size(); index++) {
            Object value = params.get(index);
            if (value instanceof BigDecimal) {
                ps.setBigDecimal(index + 1, (BigDecimal) value);
            } else if (value instanceof Integer) {
                ps.setInt(index + 1, (Integer) value);
            } else {
                ps.setObject(index + 1, value);
            }
        }
    }

    private SQLException enrichSchemaError(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();

        if (message.contains("ORA-00904") || message.toLowerCase().contains("invalid identifier")) {
            return new SQLException(
                    "Lỗi cấu trúc bảng SAN_PHAM. App đang dùng đúng các cột: MASP, TENSP, DVT, GIA, SL_TON, CREATED_AT, IS_DELETED. "
                            + "Nếu vẫn báo ORA-00904 thì kiểm tra lại schema/user trong db.properties hoặc chạy lại script tạo bảng SAN_PHAM. Chi tiết: "
                            + message,
                    e
            );
        }

        if (message.contains("ORA-00942") || message.toLowerCase().contains("table or view does not exist")) {
            return new SQLException(
                    "Không tìm thấy bảng SAN_PHAM trong schema hiện tại. Kiểm tra user/url trong db.properties hoặc tạo bảng SAN_PHAM trước. Chi tiết: "
                            + message,
                    e
            );
        }

        return e;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
