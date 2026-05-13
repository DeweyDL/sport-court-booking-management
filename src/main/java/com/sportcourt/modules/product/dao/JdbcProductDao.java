package com.sportcourt.modules.product.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.entity.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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
        List<Object> params = new ArrayList<Object>();

        sql.append("SELECT MASP, TENSP, DVT, GIA, SL_TON, CREATED_AT, NVL(IS_DELETED, 0) AS IS_DELETED ");
        sql.append("FROM SAN_PHAM ");
        sql.append("WHERE 1 = 1 ");

        if (!Boolean.TRUE.equals(safeCriteria.getIncludeDeleted())) {
            sql.append("AND NVL(IS_DELETED, 0) = 0 ");
        }

        if (!isBlank(safeCriteria.getKeyword())) {
            String keyword = "%" + safeCriteria.getKeyword().trim() + "%";
            sql.append("AND (");
            sql.append("LOWER(MASP) LIKE LOWER(?) ");
            sql.append("OR LOWER(TENSP) LIKE LOWER(?) ");
            sql.append("OR LOWER(DVT) LIKE LOWER(?)");
            sql.append(") ");

            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        sql.append("ORDER BY NVL(IS_DELETED, 0), CREATED_AT DESC, MASP DESC");

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<ProductResponse> products = new ArrayList<ProductResponse>();

                while (rs.next()) {
                    products.add(mapResponse(rs));
                }

                return products;
            }
        } catch (SQLException e) {
            throw buildProductSqlException(e);
        }
    }

    @Override
    public Optional<ProductResponse> findById(String maSp) throws SQLException {
        if (isBlank(maSp)) {
            return Optional.empty();
        }

        String sql = ""
                + "SELECT MASP, TENSP, DVT, GIA, SL_TON, CREATED_AT, NVL(IS_DELETED, 0) AS IS_DELETED "
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
            throw buildProductSqlException(e);
        }
    }

    @Override
    public boolean existsByName(String tenSp, String exceptMaSp) throws SQLException {
        if (isBlank(tenSp)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<Object>();

        sql.append("SELECT COUNT(*) ");
        sql.append("FROM SAN_PHAM ");
        sql.append("WHERE LOWER(TENSP) = LOWER(?) ");
        sql.append("AND NVL(IS_DELETED, 0) = 0 ");
        params.add(tenSp.trim());

        if (!isBlank(exceptMaSp)) {
            sql.append("AND MASP <> ? ");
            params.add(exceptMaSp.trim());
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            return count(conn, sql.toString(), params) > 0;
        } catch (SQLException e) {
            throw buildProductSqlException(e);
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
            ps.setString(1, product.getMaSp()); // Dùng maSp do người dùng nhập
            ps.setString(2, product.getTenSp());
            ps.setString(3, product.getDvt());
            ps.setBigDecimal(4, product.getGia());
            ps.setInt(5, product.getSlTon() == null ? 0 : product.getSlTon());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw buildProductSqlException(e);
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
            ps.setString(2, product.getDvt());
            ps.setBigDecimal(3, product.getGia());
            ps.setInt(4, product.getSlTon() == null ? 0 : product.getSlTon());
            ps.setString(5, product.getMaSp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw buildProductSqlException(e);
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
            throw buildProductSqlException(e);
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
            throw buildProductSqlException(e);
        }
    }

    @Override
    public String generateNextMaSp() throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MASP, '\\d+$'))), 0) + 1 AS NEXT_ID " +
                "FROM SAN_PHAM WHERE REGEXP_LIKE(MASP, '^SP-\\d+$')";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "SP-" + rs.getInt("NEXT_ID");
            }
        }
        return "SP-1";
    }

    private ProductResponse mapResponse(ResultSet rs) throws SQLException {
        ProductResponse response = new ProductResponse();

        response.setMaSp(rs.getString("MASP"));
        response.setTenSp(rs.getString("TENSP"));
        response.setDvt(rs.getString("DVT"));

        BigDecimal gia = rs.getBigDecimal("GIA");
        response.setGia(gia == null ? BigDecimal.ZERO : gia);

        int slTon = rs.getInt("SL_TON");
        if (rs.wasNull()) {
            slTon = 0;
        }
        response.setSlTon(slTon);

        Date createdDate = rs.getDate("CREATED_AT");
        if (createdDate != null) {
            response.setCreatedAt(createdDate.toLocalDate().atStartOfDay());
        }

        boolean deleted = rs.getInt("IS_DELETED") == 1;
        response.setDeleted(deleted);

        if (deleted) {
            response.setTrangThai(DELETED_STATUS);
        } else if (slTon <= 0) {
            response.setTrangThai(OUT_OF_STOCK_STATUS);
        } else {
            response.setTrangThai(ACTIVE_STATUS);
        }

        return response;
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
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);

            if (value instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) value);
            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    private SQLException buildProductSqlException(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();

        if (message.contains("ORA-00904") || message.toLowerCase().contains("invalid identifier")) {
            return new SQLException(
                    "Lỗi SQL với bảng SAN_PHAM. Kiểm tra lại code đang chạy có đúng bảng/cột: "
                            + "MASP, TENSP, DVT, GIA, SL_TON, CREATED_AT, IS_DELETED. Chi tiết: " + message,
                    e
            );
        }

        if (message.contains("ORA-00942") || message.toLowerCase().contains("table or view does not exist")) {
            return new SQLException(
                    "Không tìm thấy bảng SAN_PHAM trong schema app đang kết nối. Kiểm tra db.properties. Chi tiết: " + message,
                    e
            );
        }

        return e;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
