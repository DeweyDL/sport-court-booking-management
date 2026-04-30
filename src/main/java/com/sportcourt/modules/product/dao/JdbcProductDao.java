package com.sportcourt.modules.product.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.entity.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            ColumnMap columns = getColumnMap(conn);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(columns.maSp).append(" AS MASP, ");
            sql.append(columns.tenSp).append(" AS TENSP, ");
            sql.append(selectNullableString(columns.danhMuc)).append(" AS DANH_MUC, ");
            sql.append(selectNullableNumber(columns.gia)).append(" AS GIA_VALUE, ");
            sql.append(selectNullableNumber(columns.soLuongTon)).append(" AS SO_LUONG_TON, ");
            sql.append(selectDeleted(columns.isDeleted)).append(" AS IS_DELETED ");
            sql.append("FROM SAN_PHAM ");
            sql.append("WHERE 1 = 1 ");

            List<Object> params = new ArrayList<>();

            if (!Boolean.TRUE.equals(safeCriteria.getIncludeDeleted()) && !isBlank(columns.isDeleted)) {
                sql.append("AND NVL(").append(columns.isDeleted).append(", 0) = 0 ");
            }

            if (!isBlank(safeCriteria.getKeyword())) {
                String keyword = "%" + safeCriteria.getKeyword().trim() + "%";

                sql.append("AND (");
                sql.append("LOWER(").append(columns.maSp).append(") LIKE LOWER(?) ");
                sql.append("OR LOWER(").append(columns.tenSp).append(") LIKE LOWER(?) ");

                params.add(keyword);
                params.add(keyword);

                if (!isBlank(columns.danhMuc)) {
                    sql.append("OR LOWER(").append(columns.danhMuc).append(") LIKE LOWER(?) ");
                    params.add(keyword);
                }

                sql.append(") ");
            }

            if (!isBlank(safeCriteria.getDanhMuc()) && !isBlank(columns.danhMuc)) {
                sql.append("AND LOWER(").append(columns.danhMuc).append(") = LOWER(?) ");
                params.add(safeCriteria.getDanhMuc().trim());
            }

            sql.append("ORDER BY ");

            if (!isBlank(columns.isDeleted)) {
                sql.append("NVL(").append(columns.isDeleted).append(", 0), ");
            }

            if (!isBlank(columns.createdAt)) {
                sql.append(columns.createdAt).append(" DESC, ");
            }

            sql.append(columns.maSp).append(" DESC");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                bindParams(ps, params);

                try (ResultSet rs = ps.executeQuery()) {
                    List<ProductResponse> products = new ArrayList<>();

                    while (rs.next()) {
                        products.add(mapResponse(rs));
                    }

                    return products;
                }
            }
        }
    }

    @Override
    public Optional<ProductResponse> findById(String maSp) throws SQLException {
        if (isBlank(maSp)) {
            return Optional.empty();
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            ColumnMap columns = getColumnMap(conn);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(columns.maSp).append(" AS MASP, ");
            sql.append(columns.tenSp).append(" AS TENSP, ");
            sql.append(selectNullableString(columns.danhMuc)).append(" AS DANH_MUC, ");
            sql.append(selectNullableNumber(columns.gia)).append(" AS GIA_VALUE, ");
            sql.append(selectNullableNumber(columns.soLuongTon)).append(" AS SO_LUONG_TON, ");
            sql.append(selectDeleted(columns.isDeleted)).append(" AS IS_DELETED ");
            sql.append("FROM SAN_PHAM ");
            sql.append("WHERE ").append(columns.maSp).append(" = ?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setString(1, maSp.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResponse(rs));
                    }

                    return Optional.empty();
                }
            }
        }
    }

    @Override
    public boolean existsByName(String tenSp, String exceptMaSp) throws SQLException {
        if (isBlank(tenSp)) {
            return false;
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            ColumnMap columns = getColumnMap(conn);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM SAN_PHAM ");
            sql.append("WHERE LOWER(").append(columns.tenSp).append(") = LOWER(?) ");

            List<Object> params = new ArrayList<>();
            params.add(tenSp.trim());

            if (!isBlank(columns.isDeleted)) {
                sql.append("AND NVL(").append(columns.isDeleted).append(", 0) = 0 ");
            }

            if (!isBlank(exceptMaSp)) {
                sql.append("AND ").append(columns.maSp).append(" <> ? ");
                params.add(exceptMaSp.trim());
            }

            return count(conn, sql.toString(), params) > 0;
        }
    }

    @Override
    public void insert(Connection conn, Product product) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        ColumnMap columns = getColumnMap(conn);
        String maSp = nextProductId(conn, columns.maSp);

        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        columnNames.add(columns.maSp);
        values.add(maSp);

        columnNames.add(columns.tenSp);
        values.add(product.getTenSp());

        if (!isBlank(columns.danhMuc)) {
            columnNames.add(columns.danhMuc);
            values.add(product.getDanhMuc());
        }

        if (!isBlank(columns.gia)) {
            columnNames.add(columns.gia);
            values.add(product.getGia());
        }

        if (!isBlank(columns.soLuongTon)) {
            columnNames.add(columns.soLuongTon);
            values.add(product.getSoLuongTon() == null ? 0 : product.getSoLuongTon());
        }

        if (!isBlank(columns.createdAt)) {
            columnNames.add(columns.createdAt);
        }

        if (!isBlank(columns.isDeleted)) {
            columnNames.add(columns.isDeleted);
            values.add(0);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO SAN_PHAM (");

        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }

            sql.append(columnNames.get(i));
        }

        sql.append(") VALUES (");

        int valueIndex = 0;

        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }

            if (columnNames.get(i).equals(columns.createdAt)) {
                sql.append("SYSDATE");
            } else {
                sql.append("?");
                valueIndex++;
            }
        }

        sql.append(")");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, values);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean update(Connection conn, Product product) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        ColumnMap columns = getColumnMap(conn);

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("UPDATE SAN_PHAM SET ");
        sql.append(columns.tenSp).append(" = ? ");
        params.add(product.getTenSp());

        if (!isBlank(columns.danhMuc)) {
            sql.append(", ").append(columns.danhMuc).append(" = ? ");
            params.add(product.getDanhMuc());
        }

        if (!isBlank(columns.gia)) {
            sql.append(", ").append(columns.gia).append(" = ? ");
            params.add(product.getGia());
        }

        if (!isBlank(columns.soLuongTon)) {
            sql.append(", ").append(columns.soLuongTon).append(" = ? ");
            params.add(product.getSoLuongTon() == null ? 0 : product.getSoLuongTon());
        }

        sql.append("WHERE ").append(columns.maSp).append(" = ? ");
        params.add(product.getMaSp());

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDelete(Connection conn, String maSp) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        ColumnMap columns = getColumnMap(conn);

        if (isBlank(columns.isDeleted)) {
            throw new SQLException("Bảng SAN_PHAM không có cột IS_DELETED để xoá mềm.");
        }

        String sql = "UPDATE SAN_PHAM SET " + columns.isDeleted + " = 1 WHERE " + columns.maSp + " = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSp);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean restore(Connection conn, String maSp) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection không hợp lệ.");
        }

        ColumnMap columns = getColumnMap(conn);

        if (isBlank(columns.isDeleted)) {
            throw new SQLException("Bảng SAN_PHAM không có cột IS_DELETED để khôi phục.");
        }

        String sql = "UPDATE SAN_PHAM SET " + columns.isDeleted + " = 0 WHERE " + columns.maSp + " = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSp);
            return ps.executeUpdate() > 0;
        }
    }

    private ProductResponse mapResponse(ResultSet rs) throws SQLException {
        ProductResponse response = new ProductResponse();

        response.setMaSp(rs.getString("MASP"));
        response.setTenSp(rs.getString("TENSP"));
        response.setDanhMuc(rs.getString("DANH_MUC"));

        BigDecimal gia = rs.getBigDecimal("GIA_VALUE");
        response.setGia(gia == null ? BigDecimal.ZERO : gia);

        int soLuong = rs.getInt("SO_LUONG_TON");
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

    private ColumnMap getColumnMap(Connection conn) throws SQLException {
        ColumnMap columns = new ColumnMap();

        columns.maSp = firstExistingColumn(conn, "SAN_PHAM", "MASP", "MA_SP", "PRODUCT_ID");
        columns.tenSp = firstExistingColumn(conn, "SAN_PHAM", "TENSP", "TEN_SP", "TEN_SAN_PHAM", "TENSANPHAM");
        columns.danhMuc = firstExistingColumn(conn, "SAN_PHAM", "DVT", "DANHMUC", "DANH_MUC", "LOAI", "LOAISP");
        columns.gia = firstExistingColumn(conn, "SAN_PHAM", "GIA", "DONGIA", "DON_GIA", "GIA_BAN");
        columns.soLuongTon = firstExistingColumn(
                conn,
                "SAN_PHAM",
                "SL_TON",
                "SLTON",
                "SO_LUONG",
                "SOLUONG",
                "SL",
                "SO_LUONG_TON",
                "SOLUONGTON",
                "TON_KHO",
                "TONKHO",
                "SL_CON",
                "SLCON"
        );
        columns.createdAt = firstExistingColumn(conn, "SAN_PHAM", "CREATED_AT", "NGAY_TAO", "CREATED_DATE");
        columns.isDeleted = firstExistingColumn(conn, "SAN_PHAM", "IS_DELETED", "ISDELETE", "DA_XOA", "DELETED");

        if (isBlank(columns.maSp)) {
            throw new SQLException("Không tìm thấy cột mã sản phẩm trong SAN_PHAM. Cần có MASP hoặc MA_SP.");
        }

        if (isBlank(columns.tenSp)) {
            throw new SQLException("Không tìm thấy cột tên sản phẩm trong SAN_PHAM. Cần có TENSP hoặc TEN_SP.");
        }

        if (isBlank(columns.soLuongTon)) {
            throw new SQLException("Không tìm thấy cột số lượng tồn trong SAN_PHAM. Hãy kiểm tra tên cột thực tế bằng: SELECT COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'SAN_PHAM';");
        }

        return columns;
    }

    private String firstExistingColumn(Connection conn, String tableName, String... columnNames) throws SQLException {
        for (String columnName : columnNames) {
            if (hasColumn(conn, tableName, columnName)) {
                return columnName;
            }
        }

        return null;
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            if (rs.next()) {
                return true;
            }
        }

        String sql = ""
                + "SELECT COUNT(*) "
                + "FROM USER_TAB_COLUMNS "
                + "WHERE TABLE_NAME = ? "
                + "AND COLUMN_NAME = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase());
            ps.setString(2, columnName.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        }
    }

    private String selectNullableString(String columnName) {
        if (isBlank(columnName)) {
            return "CAST(NULL AS VARCHAR2(100))";
        }

        return columnName;
    }

    private String selectNullableNumber(String columnName) {
        if (isBlank(columnName)) {
            return "CAST(0 AS NUMBER)";
        }

        return columnName;
    }

    private String selectDeleted(String columnName) {
        if (isBlank(columnName)) {
            return "CAST(0 AS NUMBER)";
        }

        return "NVL(" + columnName + ", 0)";
    }

    private String nextProductId(Connection conn, String maSpColumn) throws SQLException {
        int max = 0;
        String sql = "SELECT " + maSpColumn + " AS MASP FROM SAN_PHAM";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String maSp = rs.getString("MASP");
                int number = extractNumber(maSp);

                if (number > max) {
                    max = number;
                }
            }
        }

        return String.format("SP%02d", max + 1);
    }

    private int extractNumber(String value) {
        if (value == null) {
            return 0;
        }

        String digits = value.replaceAll("\\D", "");

        if (digits.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int count(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class ColumnMap {
        private String maSp;
        private String tenSp;
        private String danhMuc;
        private String gia;
        private String soLuongTon;
        private String createdAt;
        private String isDeleted;
    }
}
