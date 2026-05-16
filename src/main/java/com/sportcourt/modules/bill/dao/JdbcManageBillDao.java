package com.sportcourt.modules.bill.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.CourtRentalItem;
import com.sportcourt.modules.bill.dto.ServiceItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcManageBillDao implements ManageBillDao {

    @Override
    public List<BillSummary> findAll(String keyword, String branchId) throws SQLException {
        boolean filterBranch = branchId != null && !branchId.isBlank();
        String sql = """
                SELECT hd.MAHD, hd.MAKH, u_kh.HOTEN AS TEN_KHACH_HANG,
                       hd.MANV, u_nv.HOTEN AS TEN_NHAN_VIEN,
                       hd.TIEN_COC, hd.GIAMGIA, hd.TONGGIATRI, hd.TRANGTHAI,
                       hd.TONGTIEN, hd.CREATED_AT
                FROM HOA_DON hd
                LEFT JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
                LEFT JOIN USERS u_kh ON u_kh.USER_ID = kh.USER_ID
                LEFT JOIN NHAN_VIEN nv ON nv.MANV = hd.MANV
                LEFT JOIN USERS u_nv ON u_nv.USER_ID = nv.USER_ID
                WHERE NVL(hd.IS_DELETED, 0) = 0
                  AND (
                      UPPER(hd.MAHD) LIKE '%' || UPPER(?) || '%'
                      OR UPPER(u_kh.HOTEN) LIKE '%' || UPPER(?) || '%'
                      OR hd.MAKH LIKE '%' || ? || '%'
                  )
                """
                + (filterBranch ? "  AND nv.MACN = ?\n" : "")
                + "ORDER BY hd.CREATED_AT DESC";
        List<BillSummary> result = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String kw = keyword == null ? "" : keyword.trim();
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            stmt.setString(3, kw);
            if (filterBranch) {
                stmt.setString(4, branchId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new BillSummary(
                            rs.getString("MAHD"),
                            rs.getString("MAKH"),
                            rs.getString("TEN_KHACH_HANG"),
                            rs.getString("MANV"),
                            rs.getString("TEN_NHAN_VIEN"),
                            rs.getBigDecimal("TIEN_COC"),
                            rs.getBigDecimal("GIAMGIA"),
                            rs.getBigDecimal("TONGGIATRI"),
                            trimmed(rs.getString("TRANGTHAI")),
                            rs.getBigDecimal("TONGTIEN"),
                            toLocalDateTime(rs.getTimestamp("CREATED_AT"))
                    ));
                }
            }
        }
        return result;
    }

    @Override
    public Optional<BillDetail> findDetailById(String maHD) throws SQLException {
        String masterSql = """
                SELECT hd.MAHD, hd.MAKH, u_kh.HOTEN AS TEN_KHACH_HANG, u_kh.SDT AS SDT_KHACH_HANG,
                       hd.MANV, u_nv.HOTEN AS TEN_NHAN_VIEN,
                       hd.TIEN_COC, hd.GIAMGIA, hd.TONGGIATRI, TRIM(hd.TRANGTHAI) AS TRANGTHAI,
                       hd.TONGTIEN, hd.CREATED_AT
                FROM HOA_DON hd
                LEFT JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
                LEFT JOIN USERS u_kh ON u_kh.USER_ID = kh.USER_ID
                LEFT JOIN NHAN_VIEN nv ON nv.MANV = hd.MANV
                LEFT JOIN USERS u_nv ON u_nv.USER_ID = nv.USER_ID
                WHERE hd.MAHD = ? AND NVL(hd.IS_DELETED, 0) = 0
                """;
        String courtSql = """
                SELECT ct.MACT_THUE_SAN, ct.MASAN, ct.MABG, ct.NGAYTHUE,
                       NVL(bg.GIOBATDAU, 0) AS GIOBATDAU,
                       NVL(bg.GIOKETTHUC, 0) AS GIOKETTHUC,
                       ct.DON_GIA_THUE, TRIM(ct.TRANGTHAI) AS TRANGTHAI
                FROM CHI_TIET_HOA_DON_THUE_SAN ct
                LEFT JOIN BANG_GIA bg ON bg.MABG = ct.MABG AND NVL(bg.IS_DELETED, 0) = 0
                WHERE ct.MAHD = ? AND NVL(ct.IS_DELETED, 0) = 0
                ORDER BY ct.NGAYTHUE ASC, bg.GIOBATDAU ASC
                """;
        String serviceSql = """
                SELECT ct.MACT_DICH_VU, ct.MAHD, ct.MASP, ct.MADC,
                       NVL(sp.TENSP, dc.TENDC) AS TEN_SAN_PHAM,
                       ct.SL, ct.DON_GIA, ct.TRANGTHAI
                FROM CHI_TIET_HOA_DON_DICH_VU_DA_DUNG ct
                LEFT JOIN SAN_PHAM sp ON sp.MASP = ct.MASP AND NVL(sp.IS_DELETED, 0) = 0
                LEFT JOIN DUNG_CU_THE_THAO dc ON dc.MADC = ct.MADC AND NVL(dc.IS_DELETED, 0) = 0
                WHERE ct.MAHD = ? AND NVL(ct.IS_DELETED, 0) = 0
                ORDER BY ct.CREATED_AT ASC
                """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            String mahd, makh, tenKhachHang, sdtKhachHang, manv, tenNhanVien, trangThai;
            BigDecimal tienCoc, giamGia, tongGiaTri, tongTien;
            LocalDateTime createdAt;

            try (PreparedStatement stmt = conn.prepareStatement(masterSql)) {
                stmt.setString(1, maHD);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    mahd = rs.getString("MAHD");
                    makh = rs.getString("MAKH");
                    tenKhachHang = rs.getString("TEN_KHACH_HANG");
                    sdtKhachHang = rs.getString("SDT_KHACH_HANG");
                    manv = rs.getString("MANV");
                    tenNhanVien = rs.getString("TEN_NHAN_VIEN");
                    tienCoc = rs.getBigDecimal("TIEN_COC");
                    giamGia = rs.getBigDecimal("GIAMGIA");
                    tongGiaTri = rs.getBigDecimal("TONGGIATRI");
                    trangThai = rs.getString("TRANGTHAI");
                    tongTien = rs.getBigDecimal("TONGTIEN");
                    createdAt = toLocalDateTime(rs.getTimestamp("CREATED_AT"));
                }
            }

            List<CourtRentalItem> courts = fetchCourtRentals(conn, courtSql, maHD);
            List<ServiceItem> services = fetchServiceItems(conn, serviceSql, maHD);
            return Optional.of(new BillDetail(
                    mahd, makh, tenKhachHang, sdtKhachHang,
                    manv, tenNhanVien,
                    tienCoc, giamGia, tongGiaTri, trangThai, tongTien,
                    createdAt, courts, services
            ));
        }
    }

    @Override
    public boolean updateStatus(String maHD, String newStatus, String requiredCurrentStatus) throws SQLException {
        String sql = """
                UPDATE HOA_DON SET TRANGTHAI = ?
                WHERE MAHD = ? AND TRIM(TRANGTHAI) = TRIM(?) AND NVL(IS_DELETED, 0) = 0
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, maHD);
            stmt.setString(3, requiredCurrentStatus);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDelete(String maHD) throws SQLException {
        String sql = """
                UPDATE HOA_DON SET IS_DELETED = 1
                WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            return stmt.executeUpdate() > 0;
        }
    }

    private List<CourtRentalItem> fetchCourtRentals(Connection conn, String sql, String maHD) throws SQLException {
        List<CourtRentalItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new CourtRentalItem(
                            rs.getString("MACT_THUE_SAN"),
                            rs.getString("MASAN"),
                            rs.getString("MABG"),
                            toLocalDateTime(rs.getTimestamp("NGAYTHUE")),
                            rs.getInt("GIOBATDAU"),
                            rs.getInt("GIOKETTHUC"),
                            rs.getBigDecimal("DON_GIA_THUE"),
                            rs.getString("TRANGTHAI")
                    ));
                }
            }
        }
        return items;
    }

    @Override
    public String createEmptyBill(String maKH, String maNV) throws SQLException {
        String maHd = generateNextId("HOA_DON", "MAHD", "HD-");
        String sql = """
                INSERT INTO HOA_DON (MAHD, MAKH, MANV, TIEN_COC, GIAMGIA, TONGGIATRI, TRANGTHAI, TONGTIEN, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, 0, 0, 0, 'CHƯA THANH TOÁN', 0, SYSDATE, 0)
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHd);
            stmt.setString(2, maKH);
            stmt.setString(3, maNV);
            stmt.executeUpdate();
        }
        return maHd;
    }

    @Override
    public void addServiceItems(String maHD, List<ServiceItem> items) throws SQLException {
        if (items == null || items.isEmpty()) return;
        String sql = """
                INSERT INTO CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
                    (MACT_DICH_VU, MAHD, MASP, MADC, SL, DON_GIA, TRANGTHAI, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, ?, ?, ?, 'ĐANG SỬ DỤNG', SYSDATE, 0)
                """;
        for (ServiceItem item : items) {
            if (item.soLuong() <= 0) continue;
            String maCt = generateNextId("CHI_TIET_HOA_DON_DICH_VU_DA_DUNG", "MACT_DICH_VU", "CTHDDV-");
            try (Connection conn = ConnectionUtils.getMyConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, maCt);
                stmt.setString(2, maHD);
                if (item.maSP() != null) stmt.setString(3, item.maSP());
                else stmt.setNull(3, Types.VARCHAR);
                if (item.maDC() != null) stmt.setString(4, item.maDC());
                else stmt.setNull(4, Types.VARCHAR);
                stmt.setInt(5, item.soLuong());
                stmt.setBigDecimal(6, item.donGia() == null ? BigDecimal.ZERO : item.donGia());
                stmt.executeUpdate();
            }
        }
    }

    private String generateNextId(String tableName, String idColumn, String prefix) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(" + idColumn + ", '\\d+$'))), 0) + 1 AS NEXT_ID "
                + "FROM " + tableName + " WHERE " + idColumn + " LIKE ?";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return prefix + rs.getLong("NEXT_ID");
            }
        }
        throw new SQLException("Không thể sinh mã " + tableName + ".");
    }

    private List<ServiceItem> fetchServiceItems(Connection conn, String sql, String maHD) throws SQLException {
        List<ServiceItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new ServiceItem(
                            rs.getString("MACT_DICH_VU"),
                            rs.getString("MASP"),
                            rs.getString("MADC"),
                            rs.getString("TEN_SAN_PHAM"),
                            rs.getInt("SL"),
                            rs.getBigDecimal("DON_GIA"),
                            rs.getString("TRANGTHAI")
                    ));
                }
            }
        }
        return items;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static String trimmed(String s) {
        return s == null ? null : s.trim();
    }
}
