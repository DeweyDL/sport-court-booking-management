package com.sportcourt.modules.bill.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.CourtRentalItem;
import com.sportcourt.modules.bill.dto.ServiceItem;

import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class JdbcManageBillDao implements ManageBillDao {
    private static final String COURT_STATUS_CONFIRMED = "ĐÃ XÁC NHẬN";
    private static final String COURT_STATUS_WAITING_DEPOSIT = "ĐÃ ĐẶT CHỜ CỌC";
    private static final String COURT_STATUS_DEPOSITED = "ĐÃ CỌC";
    private static final String COURT_STATUS_IN_USE = "ĐANG SỬ DỤNG";
    private static final String COURT_STATUS_CANCELLED = "ĐÃ HUỶ";

    @Override
    public List<BillSummary> findAll(String keyword, String branchId) throws SQLException {
        boolean filterBranch = branchId != null && !branchId.isBlank();
        String sql = "SELECT " + (filterBranch ? "DISTINCT " : "") + """
                       hd.MAHD, hd.MAKH, u_kh.HOTEN AS TEN_KHACH_HANG,
                       hd.MANV, u_nv.HOTEN AS TEN_NHAN_VIEN,
                       hd.TIEN_COC, hd.GIAMGIA, hd.TONGGIATRI, hd.TRANGTHAI,
                       hd.TONGTIEN, hd.CREATED_AT,
                       cn.MACN AS MACN_CHI_NHANH, cn.TEN_CHI_NHANH
                FROM HOA_DON hd
                LEFT JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
                LEFT JOIN USERS u_kh ON u_kh.USER_ID = kh.USER_ID
                LEFT JOIN NHAN_VIEN nv ON nv.MANV = hd.MANV
                LEFT JOIN USERS u_nv ON u_nv.USER_ID = nv.USER_ID
                LEFT JOIN CHI_NHANH cn ON cn.MACN = nv.MACN AND NVL(cn.IS_DELETED, 0) = 0
                """
                + (filterBranch ? """
                LEFT JOIN CHI_TIET_HOA_DON_THUE_SAN ct_branch
                       ON ct_branch.MAHD = hd.MAHD
                      AND NVL(ct_branch.IS_DELETED, 0) = 0
                LEFT JOIN SAN_CON sc_branch
                       ON sc_branch.MASAN = ct_branch.MASAN
                      AND NVL(sc_branch.IS_DELETED, 0) = 0
                LEFT JOIN KHU_VUC kv_branch
                       ON kv_branch.MAKV = sc_branch.MAKV
                      AND NVL(kv_branch.IS_DELETED, 0) = 0
                """ : "")
                + """
                WHERE NVL(hd.IS_DELETED, 0) = 0
                  AND (
                      ? IS NULL
                      OR UPPER(NVL(hd.MAHD, '')) LIKE ?
                      OR UPPER(NVL(hd.MAKH, '')) LIKE ?
                      OR UPPER(NVL(u_kh.HOTEN, '')) LIKE ?
                      OR UPPER(NVL(u_kh.SDT, '')) LIKE ?
                      OR UPPER(NVL(u_kh.EMAIL, '')) LIKE ?
                      OR UPPER(NVL(hd.MANV, '')) LIKE ?
                      OR UPPER(NVL(u_nv.HOTEN, '')) LIKE ?
                      OR UPPER(NVL(u_nv.SDT, '')) LIKE ?
                      OR UPPER(NVL(TRIM(hd.TRANGTHAI), '')) LIKE ?
                      OR UPPER(NVL(cn.MACN, '')) LIKE ?
                      OR UPPER(NVL(cn.TEN_CHI_NHANH, '')) LIKE ?
                      OR TO_CHAR(NVL(hd.TONGGIATRI, 0)) LIKE ?
                      OR TO_CHAR(NVL(hd.TONGTIEN, 0)) LIKE ?
                      OR TO_CHAR(hd.CREATED_AT, 'DD/MM/YYYY') LIKE ?
                  )
                """
                + (filterBranch ? """
                  AND (
                      nv.MACN = ?
                      OR kv_branch.MACN = ?
                  )
                """ : "")
                + "ORDER BY hd.CREATED_AT DESC";
        List<BillSummary> result = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String normalizedKeyword = normalizeKeyword(keyword);
            String likeValue = toLikeValue(normalizedKeyword);
            int paramIndex = 1;
            stmt.setString(paramIndex++, normalizedKeyword);
            for (int i = 0; i < 14; i++) {
                stmt.setString(paramIndex++, likeValue);
            }
            if (filterBranch) {
                stmt.setString(paramIndex++, branchId);
                stmt.setString(paramIndex, branchId);
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
                            toLocalDateTime(rs.getTimestamp("CREATED_AT")),
                            rs.getString("MACN_CHI_NHANH"),
                            rs.getString("TEN_CHI_NHANH")
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
                       COALESCE((
                           SELECT MIN(kv_detail.MACN)
                           FROM CHI_TIET_HOA_DON_THUE_SAN ct_detail
                           JOIN SAN_CON sc_detail
                             ON sc_detail.MASAN = ct_detail.MASAN
                            AND NVL(sc_detail.IS_DELETED, 0) = 0
                           JOIN KHU_VUC kv_detail
                             ON kv_detail.MAKV = sc_detail.MAKV
                            AND NVL(kv_detail.IS_DELETED, 0) = 0
                           WHERE ct_detail.MAHD = hd.MAHD
                             AND NVL(ct_detail.IS_DELETED, 0) = 0
                       ), nv.MACN) AS MACN,
                       hd.TIEN_COC, hd.GIAMGIA, hd.TONGGIATRI, TRIM(hd.TRANGTHAI) AS TRANGTHAI,
                       hd.TONGTIEN, hd.CREATED_AT, NVL(hk.CHIET_KHAU, 0) AS CHIET_KHAU
                FROM HOA_DON hd
                LEFT JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
                LEFT JOIN HANG_KHACH_HANG hk ON kh.MA_HANG = hk.MA_HANG AND NVL(hk.IS_DELETED, 0) = 0
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
            String mahd, makh, tenKhachHang, sdtKhachHang, manv, tenNhanVien, macn, trangThai;
            BigDecimal tienCoc, giamGia, tongGiaTri, tongTien, chietKhau;
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
                    macn = rs.getString("MACN");
                    tienCoc = rs.getBigDecimal("TIEN_COC");
                    giamGia = rs.getBigDecimal("GIAMGIA");
                    tongGiaTri = rs.getBigDecimal("TONGGIATRI");
                    trangThai = rs.getString("TRANGTHAI");
                    tongTien = rs.getBigDecimal("TONGTIEN");
                    chietKhau = rs.getBigDecimal("CHIET_KHAU");
                    createdAt = toLocalDateTime(rs.getTimestamp("CREATED_AT"));
                }
            }

            List<CourtRentalItem> courts = fetchCourtRentals(conn, courtSql, maHD);
            List<ServiceItem> services = fetchServiceItems(conn, serviceSql, maHD);
            return Optional.of(new BillDetail(
                    mahd, makh, tenKhachHang, sdtKhachHang,
                    manv, tenNhanVien, macn,
                    tienCoc, giamGia, tongGiaTri, trangThai, tongTien, chietKhau,
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
        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (isPayingInvoice(newStatus, requiredCurrentStatus)) {
                    startConfirmedCourtDetails(conn, maHD);
                }

                stmt.setString(1, newStatus);
                stmt.setString(2, maHD);
                stmt.setString(3, requiredCurrentStatus);
                boolean updated = stmt.executeUpdate() > 0;
                if (updated) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return updated;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private boolean isPayingInvoice(String newStatus, String requiredCurrentStatus) {
        return "ĐÃ THANH TOÁN".equals(trimmed(newStatus))
                && "CHƯA THANH TOÁN".equals(trimmed(requiredCurrentStatus));
    }

    private void startConfirmedCourtDetails(Connection conn, String maHD) throws SQLException {
        String startConfirmedSql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN
                SET TRANGTHAI = ?
                WHERE MAHD = ?
                  AND NVL(IS_DELETED, 0) = 0
                  AND TRIM(TRANGTHAI) = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(startConfirmedSql)) {
            stmt.setString(1, COURT_STATUS_IN_USE);
            stmt.setString(2, maHD);
            stmt.setString(3, COURT_STATUS_CONFIRMED);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean markDepositPaid(String maHD) throws SQLException {
        String sql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN CT
                SET CT.TRANGTHAI = ?
                WHERE CT.MAHD = ?
                  AND NVL(CT.IS_DELETED, 0) = 0
                  AND TRIM(CT.TRANGTHAI) = ?
                  AND EXISTS (
                      SELECT 1
                      FROM HOA_DON HD
                      WHERE HD.MAHD = CT.MAHD
                        AND NVL(HD.IS_DELETED, 0) = 0
                        AND NVL(HD.TIEN_COC, 0) > 0
                        AND TRIM(HD.TRANGTHAI) = 'CHƯA THANH TOÁN'
                  )
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, COURT_STATUS_DEPOSITED);
            stmt.setString(2, maHD);
            stmt.setString(3, COURT_STATUS_WAITING_DEPOSIT);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDelete(String maHD) throws SQLException {
        String softDeleteServiceSql = """
                UPDATE CHI_TIET_HOA_DON_DICH_VU_DA_DUNG
                SET IS_DELETED = 1
                WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0
                """;
        String softDeleteCourtSql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN
                SET IS_DELETED = 1
                WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0
                """;
        String softDeleteBillSql = """
                UPDATE HOA_DON
                SET IS_DELETED = 1
                WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0
                """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            boolean suppressingRecalc = false;
            try {
                if (!lockActiveInvoice(conn, maHD)) {
                    conn.rollback();
                    return false;
                }

                executeSoftDeleteByInvoice(conn, softDeleteServiceSql, maHD);

                setCourtRecalcSuppressed(conn, true);
                suppressingRecalc = true;
                executeSoftDeleteByInvoice(conn, softDeleteCourtSql, maHD);
                int deletedBills = executeSoftDeleteByInvoice(conn, softDeleteBillSql, maHD);
                setCourtRecalcSuppressed(conn, false);
                suppressingRecalc = false;

                conn.commit();
                return deletedBills > 0;
            } catch (SQLException | RuntimeException e) {
                if (suppressingRecalc) {
                    try {
                        setCourtRecalcSuppressed(conn, false);
                    } catch (SQLException ignored) {
                        // Keep the original database error visible to the caller.
                    }
                }
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private boolean lockActiveInvoice(Connection conn, String maHD) throws SQLException {
        String sql = """
                SELECT 1
                FROM HOA_DON
                WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0
                FOR UPDATE
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int executeSoftDeleteByInvoice(Connection conn, String sql, String maHD) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            return stmt.executeUpdate();
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String toLikeValue(String keyword) {
        return keyword == null ? null : "%" + keyword + "%";
    }

    @Override
    public void addCourtBookingDetails(String maHD, List<SelectedBookingSlot> slots, boolean advanceBooking) throws SQLException {
        if (slots == null || slots.isEmpty()) return;
        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                setCourtRecalcSuppressed(conn, true);
                ensureInvoiceExists(conn, maHD);

                int nextDetailNumber = findNextNumericId(conn, "CHI_TIET_HOA_DON_THUE_SAN", "MACT_THUE_SAN", "CTHDTS-");
                for (SelectedBookingSlot slot : slots) {
                    String detailId = "CTHDTS-" + nextDetailNumber++;
                    if (advanceBooking) {
                        insertCourtRental(conn, maHD, detailId, slot, COURT_STATUS_WAITING_DEPOSIT);
                    } else {
                        insertCourtRental(conn, maHD, detailId, slot, COURT_STATUS_CONFIRMED);
                        updateCourtRentalStatus(conn, detailId, COURT_STATUS_IN_USE, COURT_STATUS_CONFIRMED);
                    }
                }
                updateInvoiceDeposit(conn, maHD, advanceBooking);
                setCourtRecalcSuppressed(conn, false);
                recalculateInvoice(conn, maHD);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    setCourtRecalcSuppressed(conn, false);
                } catch (SQLException ignored) {
                    // Keep the original database error visible to the caller.
                }
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void ensureInvoiceExists(Connection conn, String maHD) throws SQLException {
        String sql = "SELECT 1 FROM HOA_DON WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Hóa đơn không tồn tại hoặc đã bị xóa: " + maHD);
                }
            }
        }
    }

    private void insertCourtRental(Connection conn, String maHD, String detailId, SelectedBookingSlot slot, String status) throws SQLException {
        String sql = "{call PRC_THEM_CHI_TIET_THUE_SAN(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, detailId);
            cs.setString(2, maHD);
            cs.setString(3, slot.courtId());
            cs.setString(4, slot.priceBoardId());
            cs.setDate(5, java.sql.Date.valueOf(slot.bookingDate()));
            cs.setString(6, status);
            cs.execute();
        }
    }

    private void updateCourtRentalStatus(Connection conn, String detailId, String newStatus, String requiredCurrentStatus) throws SQLException {
        String sql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN
                SET TRANGTHAI = ?
                WHERE MACT_THUE_SAN = ?
                  AND NVL(IS_DELETED, 0) = 0
                  AND TRIM(TRANGTHAI) = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, detailId);
            stmt.setString(3, requiredCurrentStatus);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Không thể chuyển trạng thái chi tiết thuê sân " + detailId + " sang " + newStatus + ".");
            }
        }
    }

    private void updateInvoiceDeposit(Connection conn, String maHD, boolean advanceBooking) throws SQLException {
        BigDecimal totalCourtRental = BigDecimal.ZERO;
        String totalSql = """
                SELECT NVL(SUM(DON_GIA_THUE), 0) AS TOTAL_COURT_RENTAL
                FROM CHI_TIET_HOA_DON_THUE_SAN
                WHERE MAHD = ?
                  AND NVL(IS_DELETED, 0) = 0
                  AND TRIM(TRANGTHAI) <> ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(totalSql)) {
            stmt.setString(1, maHD);
            stmt.setString(2, COURT_STATUS_CANCELLED);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCourtRental = rs.getBigDecimal("TOTAL_COURT_RENTAL");
                    if (totalCourtRental == null) {
                        totalCourtRental = BigDecimal.ZERO;
                    }
                }
            }
        }

        BigDecimal deposit = advanceBooking
                ? totalCourtRental.multiply(new BigDecimal("0.7")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String updateSql = "UPDATE HOA_DON SET TIEN_COC = ? WHERE MAHD = ? AND NVL(IS_DELETED, 0) = 0";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setBigDecimal(1, deposit);
            stmt.setString(2, maHD);
            stmt.executeUpdate();
        }
    }

    private void recalculateInvoice(Connection conn, String maHD) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{call PRC_CAP_NHAT_SO_TIEN_HOA_DON(?)}")) {
            cs.setString(1, maHD);
            cs.execute();
        }
    }

    private void setCourtRecalcSuppressed(Connection conn, boolean suppressed) throws SQLException {
        String sql = suppressed
                ? "BEGIN PKG_COURT_CTX.G_INTERNAL_RECALC := TRUE; END;"
                : "BEGIN PKG_COURT_CTX.G_INTERNAL_RECALC := FALSE; END;";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.execute();
        }
    }

    private int findNextNumericId(Connection connection, String tableName, String columnName, String prefix) throws SQLException {
        String sql = """
                SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(%s, '\\d+$'))), 0) + 1 AS NEXT_ID
                FROM %s
                WHERE REGEXP_LIKE(%s, '^%s\\d+$')
                """.formatted(columnName, tableName, columnName, prefix);

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("NEXT_ID") : 1;
        }
    }

    @Override
    public void updateServiceItemQty(String maCTHDDV, int newQty) throws SQLException {
        String sql;
        if (newQty < 1) {
            sql = "UPDATE CHI_TIET_HOA_DON_DICH_VU_DA_DUNG SET IS_DELETED = 1 WHERE MACT_DICH_VU = ? AND IS_DELETED = 0";
        } else {
            sql = "UPDATE CHI_TIET_HOA_DON_DICH_VU_DA_DUNG SET SL = ? WHERE MACT_DICH_VU = ? AND IS_DELETED = 0";
        }
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (newQty < 1) {
                stmt.setString(1, maCTHDDV);
            } else {
                stmt.setInt(1, newQty);
                stmt.setString(2, maCTHDDV);
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteCourtRental(String maCTHDTS) throws SQLException {
        String sql = "{call PRC_HUY_CHI_TIET_THUE_SAN(?)}";
        try (Connection conn = ConnectionUtils.getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, maCTHDTS);
            cs.execute();
        }
    }

    @Override
    public void updateDiscount(String maHD, int discountPercent) throws SQLException {
        String updateSql = "UPDATE HOA_DON SET GIAMGIA = ? WHERE MAHD = ?";
        String prcSql = "{call PRC_CAP_NHAT_SO_TIEN_HOA_DON(?)}";
        try (Connection conn = ConnectionUtils.getMyConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, discountPercent);
                stmt.setString(2, maHD);
                stmt.executeUpdate();
            }
            try (CallableStatement cs = conn.prepareCall(prcSql)) {
                cs.setString(1, maHD);
                cs.execute();
            }
        }
    }
}
