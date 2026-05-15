package com.sportcourt.modules.imports.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.imports.dto.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcImportManagementDAO implements ImportManagementDAO {

    @Override
    public List<ImportRow> findImports(String keyword) throws SQLException {
        String sql = """
                SELECT nh.MANH,
                       nh.MANCC,
                       ncc.TENNCC,
                       nh.MANV,
                       u.HOTEN AS TEN_NV,
                       nh.MACHUNGTU,
                       nh.TRI_GIA,
                       nh.CREATED_AT,
                       nh.IS_DELETED
                FROM NHAP_HANG nh
                JOIN NHA_CUNG_CAP ncc ON ncc.MANCC = nh.MANCC
                JOIN NHAN_VIEN nv ON nv.MANV = nh.MANV
                JOIN USERS u ON u.USER_ID = nv.USER_ID
                WHERE nh.IS_DELETED = 0
                  AND (
                      ? IS NULL
                      OR UPPER(nh.MANH) LIKE ?
                      OR UPPER(ncc.TENNCC) LIKE ?
                      OR UPPER(nh.MACHUNGTU) LIKE ?
                      OR UPPER(u.HOTEN) LIKE ?
                  )
                ORDER BY nh.CREATED_AT DESC, nh.MANH ASC
                """;
        String normalized = normalizeKeyword(keyword);
        List<ImportRow> rows = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, toLikeValue(normalized));
            ps.setString(3, toLikeValue(normalized));
            ps.setString(4, toLikeValue(normalized));
            ps.setString(5, toLikeValue(normalized));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapImportRow(rs));
                }
            }
        }
        return rows;
    }

    @Override
    public List<ImportProductDetailDTO> findProductDetails(String manh) throws SQLException {
        String sql = """
                SELECT ct.MACTNH_SP, ct.MANH, ct.MASP, sp.TENSP,
                       ct.SLTHEOCHUNGTU, ct.SLTHUCNHAP, ct.DONGIA, ct.VAT
                FROM CHI_TIET_NHAP_HANG ct
                JOIN SAN_PHAM sp ON sp.MASP = ct.MASP
                WHERE ct.MANH = ?
                  AND ct.IS_DELETED = 0
                ORDER BY ct.MACTNH_SP ASC
                """;
        List<ImportProductDetailDTO> rows = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportProductDetailDTO dto = new ImportProductDetailDTO();
                    dto.setMaCtnhSp(rs.getString("MACTNH_SP"));
                    dto.setManh(rs.getString("MANH"));
                    dto.setMaSp(rs.getString("MASP"));
                    dto.setTenSp(rs.getString("TENSP"));
                    dto.setSlTheoChungTu(rs.getInt("SLTHEOCHUNGTU"));
                    dto.setSlThucNhap(rs.getInt("SLTHUCNHAP"));
                    dto.setDonGia(rs.getBigDecimal("DONGIA"));
                    dto.setVat(rs.getBigDecimal("VAT"));
                    rows.add(dto);
                }
            }
        }
        return rows;
    }

    @Override
    public List<ImportEquipmentDetailDTO> findEquipmentDetails(String manh) throws SQLException {
        String sql = """
                SELECT ct.MACTNH_DC, ct.MANH, ct.MADC, dc.TENDC,
                       ct.SLTHEOCHUNGTU, ct.SLTHUCNHAP, ct.CKTM, ct.DONGIA
                FROM CHI_TIET_NHAP_DUNG_CU ct
                JOIN DUNG_CU_THE_THAO dc ON dc.MADC = ct.MADC
                WHERE ct.MANH = ?
                  AND ct.IS_DELETED = 0
                ORDER BY ct.MACTNH_DC ASC
                """;
        List<ImportEquipmentDetailDTO> rows = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manh);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportEquipmentDetailDTO dto = new ImportEquipmentDetailDTO();
                    dto.setMaCtnhDc(rs.getString("MACTNH_DC"));
                    dto.setManh(rs.getString("MANH"));
                    dto.setMaDc(rs.getString("MADC"));
                    dto.setTenDc(rs.getString("TENDC"));
                    dto.setSlTheoChungTu(rs.getInt("SLTHEOCHUNGTU"));
                    dto.setSlThucNhap(rs.getInt("SLTHUCNHAP"));
                    dto.setCktm(rs.getBigDecimal("CKTM"));
                    dto.setDonGia(rs.getBigDecimal("DONGIA"));
                    rows.add(dto);
                }
            }
        }
        return rows;
    }

    @Override
    public String generateNextImportId() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(SUBSTR(MANH, 3))), 0) + 1
                FROM NHAP_HANG
                WHERE REGEXP_LIKE(MANH, '^NH[0-9]+$')
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "NH" + String.format("%03d", rs.getInt(1));
            }
            return "NH001";
        }
    }

    @Override
    public String generateNextProductDetailId() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MACTNH_SP, '\\d+$'))), 0) + 1
                FROM CHI_TIET_NHAP_HANG
                WHERE REGEXP_LIKE(MACTNH_SP, '^CTSP[0-9]+$')
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "CTSP" + String.format("%03d", rs.getInt(1));
            }
            return "CTSP001";
        }
    }

    @Override
    public String generateNextEquipmentDetailId() throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MACTNH_DC, '\\d+$'))), 0) + 1
                FROM CHI_TIET_NHAP_DUNG_CU
                WHERE REGEXP_LIKE(MACTNH_DC, '^CTDC[0-9]+$')
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "CTDC" + String.format("%03d", rs.getInt(1));
            }
            return "CTDC001";
        }
    }

    @Override
    public void createImport(ImportCreateRequest request) throws SQLException {
        String insertNhapHang = """
                INSERT INTO NHAP_HANG (MANH, MANCC, MANV, MACHUNGTU, TRI_GIA, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertProductDetail = """
                INSERT INTO CHI_TIET_NHAP_HANG (MACTNH_SP, MANH, MASP, SLTHEOCHUNGTU, SLTHUCNHAP, DONGIA, VAT, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertEquipmentDetail = """
                INSERT INTO CHI_TIET_NHAP_DUNG_CU (MACTNH_DC, MANH, MADC, SLTHEOCHUNGTU, SLTHUCNHAP, CKTM, DONGIA, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, 0)
                """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String manh = request.getManh();

                // Insert NHAP_HANG
                try (PreparedStatement ps = conn.prepareStatement(insertNhapHang)) {
                    ps.setString(1, manh);
                    ps.setString(2, request.getMancc());
                    ps.setString(3, request.getManv());
                    ps.setString(4, request.getMaChungTu());
                    ps.setBigDecimal(5, request.getTriGia());
                    ps.executeUpdate();
                }

                // Insert product details
                if (request.getProductDetails() != null) {
                    int spSeq = getNextProductDetailSeq(conn);
                    try (PreparedStatement ps = conn.prepareStatement(insertProductDetail)) {
                        for (ImportProductDetailDTO detail : request.getProductDetails()) {
                            String detailId = "CTSP" + String.format("%03d", spSeq++);
                            ps.setString(1, detailId);
                            ps.setString(2, manh);
                            ps.setString(3, detail.getMaSp());
                            ps.setInt(4, detail.getSlTheoChungTu());
                            ps.setInt(5, detail.getSlThucNhap());
                            ps.setBigDecimal(6, detail.getDonGia());
                            ps.setBigDecimal(7, detail.getVat());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                // Insert equipment details
                if (request.getEquipmentDetails() != null) {
                    int dcSeq = getNextEquipmentDetailSeq(conn);
                    try (PreparedStatement ps = conn.prepareStatement(insertEquipmentDetail)) {
                        for (ImportEquipmentDetailDTO detail : request.getEquipmentDetails()) {
                            String detailId = "CTDC" + String.format("%03d", dcSeq++);
                            ps.setString(1, detailId);
                            ps.setString(2, manh);
                            ps.setString(3, detail.getMaDc());
                            ps.setInt(4, detail.getSlTheoChungTu());
                            ps.setInt(5, detail.getSlThucNhap());
                            ps.setBigDecimal(6, detail.getCktm());
                            ps.setBigDecimal(7, detail.getDonGia());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public void updateImport(ImportCreateRequest request) throws SQLException {
        String updateNhapHang = """
                UPDATE NHAP_HANG
                SET MANCC = ?, MANV = ?, MACHUNGTU = ?, TRI_GIA = ?
                WHERE MANH = ? AND IS_DELETED = 0
                """;
        String softDeleteProductDetails = """
                UPDATE CHI_TIET_NHAP_HANG SET IS_DELETED = 1 WHERE MANH = ?
                """;
        String softDeleteEquipmentDetails = """
                UPDATE CHI_TIET_NHAP_DUNG_CU SET IS_DELETED = 1 WHERE MANH = ?
                """;
        String insertProductDetail = """
                INSERT INTO CHI_TIET_NHAP_HANG (MACTNH_SP, MANH, MASP, SLTHEOCHUNGTU, SLTHUCNHAP, DONGIA, VAT, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, 0)
                """;
        String insertEquipmentDetail = """
                INSERT INTO CHI_TIET_NHAP_DUNG_CU (MACTNH_DC, MANH, MADC, SLTHEOCHUNGTU, SLTHUCNHAP, CKTM, DONGIA, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, 0)
                """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String manh = request.getManh();

                // Update header
                try (PreparedStatement ps = conn.prepareStatement(updateNhapHang)) {
                    ps.setString(1, request.getMancc());
                    ps.setString(2, request.getManv());
                    ps.setString(3, request.getMaChungTu());
                    ps.setBigDecimal(4, request.getTriGia());
                    ps.setString(5, manh);
                    ps.executeUpdate();
                }

                // Soft delete old product details
                try (PreparedStatement ps = conn.prepareStatement(softDeleteProductDetails)) {
                    ps.setString(1, manh);
                    ps.executeUpdate();
                }

                // Soft delete old equipment details
                try (PreparedStatement ps = conn.prepareStatement(softDeleteEquipmentDetails)) {
                    ps.setString(1, manh);
                    ps.executeUpdate();
                }

                // Re-insert product details
                if (request.getProductDetails() != null) {
                    int spSeq = getNextProductDetailSeq(conn);
                    try (PreparedStatement ps = conn.prepareStatement(insertProductDetail)) {
                        for (ImportProductDetailDTO detail : request.getProductDetails()) {
                            String detailId = "CTSP" + String.format("%03d", spSeq++);
                            ps.setString(1, detailId);
                            ps.setString(2, manh);
                            ps.setString(3, detail.getMaSp());
                            ps.setInt(4, detail.getSlTheoChungTu());
                            ps.setInt(5, detail.getSlThucNhap());
                            ps.setBigDecimal(6, detail.getDonGia());
                            ps.setBigDecimal(7, detail.getVat());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                // Re-insert equipment details
                if (request.getEquipmentDetails() != null) {
                    int dcSeq = getNextEquipmentDetailSeq(conn);
                    try (PreparedStatement ps = conn.prepareStatement(insertEquipmentDetail)) {
                        for (ImportEquipmentDetailDTO detail : request.getEquipmentDetails()) {
                            String detailId = "CTDC" + String.format("%03d", dcSeq++);
                            ps.setString(1, detailId);
                            ps.setString(2, manh);
                            ps.setString(3, detail.getMaDc());
                            ps.setInt(4, detail.getSlTheoChungTu());
                            ps.setInt(5, detail.getSlThucNhap());
                            ps.setBigDecimal(6, detail.getCktm());
                            ps.setBigDecimal(7, detail.getDonGia());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean deleteImport(String manh) throws SQLException {
        String softDeleteNh = """
                UPDATE NHAP_HANG SET IS_DELETED = 1 WHERE MANH = ? AND IS_DELETED = 0
                """;
        String softDeleteSp = """
                UPDATE CHI_TIET_NHAP_HANG SET IS_DELETED = 1 WHERE MANH = ? AND IS_DELETED = 0
                """;
        String softDeleteDc = """
                UPDATE CHI_TIET_NHAP_DUNG_CU SET IS_DELETED = 1 WHERE MANH = ? AND IS_DELETED = 0
                """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int affectedRows;
                try (PreparedStatement ps = conn.prepareStatement(softDeleteNh)) {
                    ps.setString(1, manh);
                    affectedRows = ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(softDeleteSp)) {
                    ps.setString(1, manh);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(softDeleteDc)) {
                    ps.setString(1, manh);
                    ps.executeUpdate();
                }
                conn.commit();
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public List<SupplierOption> findSupplierOptions() throws SQLException {
        String sql = """
                SELECT MANCC, TENNCC
                FROM NHA_CUNG_CAP
                WHERE IS_DELETED = 0
                ORDER BY TENNCC ASC
                """;
        List<SupplierOption> options = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(new SupplierOption(rs.getString("MANCC"), rs.getString("TENNCC")));
            }
        }
        return options;
    }

    @Override
    public List<EmployeeOption> findEmployeeOptions() throws SQLException {
        String sql = """
                SELECT nv.MANV, u.HOTEN
                FROM NHAN_VIEN nv
                JOIN USERS u ON u.USER_ID = nv.USER_ID
                WHERE nv.IS_DELETED = 0
                  AND nv.TRANG_THAI = 'ĐANG LÀM VIỆC'
                ORDER BY u.HOTEN ASC
                """;
        List<EmployeeOption> options = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(new EmployeeOption(rs.getString("MANV"), rs.getString("HOTEN")));
            }
        }
        return options;
    }

    @Override
    public List<ProductOption> findProductOptions() throws SQLException {
        String sql = """
                SELECT MASP, TENSP
                FROM SAN_PHAM
                WHERE IS_DELETED = 0
                ORDER BY TENSP ASC
                """;
        List<ProductOption> options = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(new ProductOption(rs.getString("MASP"), rs.getString("TENSP")));
            }
        }
        return options;
    }

    @Override
    public List<EquipmentOption> findEquipmentOptions() throws SQLException {
        String sql = """
                SELECT MADC, TENDC
                FROM DUNG_CU_THE_THAO
                WHERE IS_DELETED = 0
                ORDER BY TENDC ASC
                """;
        List<EquipmentOption> options = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(new EquipmentOption(rs.getString("MADC"), rs.getString("TENDC")));
            }
        }
        return options;
    }

    // --------- Private helpers ---------

    private ImportRow mapImportRow(ResultSet rs) throws SQLException {
        ImportRow row = new ImportRow();
        row.setManh(rs.getString("MANH"));
        row.setMancc(rs.getString("MANCC"));
        row.setTenNcc(rs.getString("TENNCC"));
        row.setManv(rs.getString("MANV"));
        row.setTenNv(rs.getString("TEN_NV"));
        row.setMaChungTu(rs.getString("MACHUNGTU"));
        row.setTriGia(rs.getBigDecimal("TRI_GIA"));
        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        row.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        row.setDeleted(rs.getInt("IS_DELETED") == 1);
        return row;
    }

    private int getNextProductDetailSeq(Connection conn) throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MACTNH_SP, '\\d+$'))), 0) + 1
                FROM CHI_TIET_NHAP_HANG
                WHERE REGEXP_LIKE(MACTNH_SP, '^CTSP[0-9]+$')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            return 1;
        }
    }

    private int getNextEquipmentDetailSeq(Connection conn) throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MACTNH_DC, '\\d+$'))), 0) + 1
                FROM CHI_TIET_NHAP_DUNG_CU
                WHERE REGEXP_LIKE(MACTNH_DC, '^CTDC[0-9]+$')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            return 1;
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim().toUpperCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toLikeValue(String keyword) {
        return keyword == null ? null : "%" + keyword + "%";
    }
}
