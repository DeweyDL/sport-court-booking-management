package com.sportcourt.modules.customer_history.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookingHistoryDAO implements BookingHistoryDAO {

    private static final String SQL_FIND_BY_CUSTOMER = """
            SELECT
                HD.MAHD,
                MAX(CT.TRANGTHAI)                AS TRANGTHAI,
                HD.TONGTIEN,
                HD.CREATED_AT,
                MAX(LTT.TEN)                     AS SPORT_TYPE_NAME,
                MAX(CN.TEN_CHI_NHANH)            AS BRANCH_NAME,
                MAX(CN.DIACHI)                   AS BRANCH_ADDRESS,
                MIN(CT.NGAYTHUE)                 AS FIRST_BOOKING_DATE,
                COUNT(CT.MACT_THUE_SAN)          AS COURT_COUNT
            FROM HOA_DON HD
            JOIN KHACH_HANG KH  ON KH.MAKH      = HD.MAKH
            LEFT JOIN CHI_TIET_HOA_DON_THUE_SAN CT
                ON CT.MAHD       = HD.MAHD
               AND CT.IS_DELETED = 0
            LEFT JOIN SAN_CON SC     ON SC.MASAN = CT.MASAN  AND SC.IS_DELETED = 0
            LEFT JOIN KHU_VUC KV     ON KV.MAKV  = SC.MAKV   AND KV.IS_DELETED = 0
            LEFT JOIN CHI_NHANH CN   ON CN.MACN  = KV.MACN   AND CN.IS_DELETED = 0
            LEFT JOIN LOAI_THE_THAO LTT ON LTT.MATT = KV.MATT AND LTT.IS_DELETED = 0
            WHERE (? IS NULL OR KH.MAKH = ?)
              AND HD.IS_DELETED = 0
              AND (
                    ? IS NULL
                    OR ? = ''
                    OR UPPER(HD.MAHD)           LIKE UPPER(?)
                    OR UPPER(CN.TEN_CHI_NHANH)  LIKE UPPER(?)
                    OR UPPER(LTT.TEN)           LIKE UPPER(?)
              )
            GROUP BY
                HD.MAHD, HD.TONGTIEN, HD.CREATED_AT
            ORDER BY HD.CREATED_AT DESC
            """;

    private static final String SQL_DETAIL_HEADER = """
            SELECT
                HD.MAHD,
                HD.TRANGTHAI,
                HD.TIEN_COC,
                HD.GIAMGIA,
                HD.TONGGIATRI,
                HD.TONGTIEN,
                HD.CREATED_AT,
                U.HOTEN   AS CUSTOMER_NAME,
                U.SDT     AS CUSTOMER_PHONE,
                KH.MAKH   AS CUSTOMER_ID
            FROM HOA_DON HD
            JOIN KHACH_HANG KH ON KH.MAKH    = HD.MAKH
            JOIN USERS U       ON U.USER_ID  = KH.USER_ID
            WHERE HD.MAHD       = ?
              AND HD.IS_DELETED  = 0
            """;

    private static final String SQL_DETAIL_LINES = """
            SELECT
                CT.MACT_THUE_SAN,
                CT.MASAN,
                CT.NGAYTHUE,
                CT.DON_GIA_THUE,
                CT.TRANGTHAI,
                BG.GIOBATDAU,
                BG.GIOKETTHUC,
                LTT.TEN         AS SPORT_TYPE_NAME,
                CN.TEN_CHI_NHANH AS BRANCH_NAME,
                CN.DIACHI        AS BRANCH_ADDRESS
            FROM CHI_TIET_HOA_DON_THUE_SAN CT
            JOIN BANG_GIA BG    ON BG.MABG   = CT.MABG   AND BG.IS_DELETED  = 0
            JOIN SAN_CON SC     ON SC.MASAN  = CT.MASAN  AND SC.IS_DELETED  = 0
            JOIN KHU_VUC KV     ON KV.MAKV   = SC.MAKV   AND KV.IS_DELETED  = 0
            JOIN CHI_NHANH CN   ON CN.MACN   = KV.MACN   AND CN.IS_DELETED  = 0
            JOIN LOAI_THE_THAO LTT ON LTT.MATT = KV.MATT AND LTT.IS_DELETED = 0
            WHERE CT.MAHD        = ?
              AND CT.IS_DELETED   = 0
            ORDER BY CT.NGAYTHUE, BG.GIOBATDAU
            """;

    private static final String SQL_DETAIL_SERVICES = """
            SELECT
                CT.MAHD,
                NVL(SP.TENSP, DC.TENDC) AS SERVICE_NAME,
                CT.SL AS QUANTITY,
                (CT.SL * CT.DON_GIA) AS TOTAL_PRICE
            FROM CHI_TIET_HOA_DON_DICH_VU_DA_DUNG CT
            LEFT JOIN SAN_PHAM SP ON CT.MASP = SP.MASP
            LEFT JOIN DUNG_CU_THE_THAO DC ON CT.MADC = DC.MADC
            WHERE CT.MAHD = ? AND CT.IS_DELETED = 0
            """;

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    @Override
    public void cancelCourtBooking(String detailId) {
        String sql = "{call PRC_HUY_CHI_TIET_THUE_SAN(?)}";
        try (Connection conn = ConnectionUtils.getMyConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, detailId);
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<BookingHistoryItemDTO> findByCustomerId(String customerId, String keyword) {
        List<BookingHistoryItemDTO> result = new ArrayList<>();
        String like = null;

        if (keyword != null && !keyword.isBlank()) {
            String processedKeyword = keyword.trim().replaceAll("(?i)^Sân\\s+", "");
            like = "%" + processedKeyword + "%";
        }

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CUSTOMER)) {

            ps.setString(1, customerId);
            ps.setString(2, customerId);

            if (like == null) {
                ps.setNull(3, Types.VARCHAR);
                ps.setNull(4, Types.VARCHAR);
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            } else {
                ps.setString(3, like);
                ps.setString(4, like);
                ps.setString(5, like);
                ps.setString(6, like);
                ps.setString(7, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingHistoryItemDTO dto = new BookingHistoryItemDTO();
                    dto.setInvoiceId(rs.getString("MAHD"));
                    dto.setStatus(rs.getString("TRANGTHAI"));
                    dto.setTotalAmount(rs.getBigDecimal("TONGTIEN"));
                    dto.setCreatedAt(toLocalDateTime(rs.getTimestamp("CREATED_AT")));
                    dto.setSportTypeName(rs.getString("SPORT_TYPE_NAME"));
                    dto.setBranchName(rs.getString("BRANCH_NAME"));
                    dto.setBranchAddress(rs.getString("BRANCH_ADDRESS"));
                    dto.setBookingDate(toLocalDateTime(rs.getTimestamp("FIRST_BOOKING_DATE")));
                    dto.setCourtCount(rs.getInt("COURT_COUNT"));
                    result.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn lịch sử đặt sân: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public BookingDetailDTO findDetailByInvoiceId(String invoiceId) {
        BookingDetailDTO dto = null;

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DETAIL_HEADER)) {

            ps.setString(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto = new BookingDetailDTO();
                    dto.setInvoiceId(rs.getString("MAHD"));
                    dto.setStatus(rs.getString("TRANGTHAI"));
                    dto.setDeposit(rs.getBigDecimal("TIEN_COC"));
                    dto.setDiscount(rs.getBigDecimal("GIAMGIA"));
                    dto.setTotalValue(rs.getBigDecimal("TONGGIATRI"));
                    dto.setTotalAmount(rs.getBigDecimal("TONGTIEN"));
                    dto.setCreatedAt(toLocalDateTime(rs.getTimestamp("CREATED_AT")));
                    dto.setCustomerName(rs.getString("CUSTOMER_NAME"));
                    dto.setCustomerPhone(rs.getString("CUSTOMER_PHONE"));
                    dto.setCustomerId(rs.getString("CUSTOMER_ID"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy thông tin hóa đơn: " + e.getMessage(), e);
        }

        if (dto == null) return null;

        List<BookingDetailDTO.CourtLineItem> items = new ArrayList<>();
        String lastBranch = null;
        String lastBranchAddr = null;

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DETAIL_LINES)) {

            ps.setString(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingDetailDTO.CourtLineItem item = new BookingDetailDTO.CourtLineItem();
                    item.setBookingDetailId(rs.getString("MACT_THUE_SAN"));
                    item.setCourtId(rs.getString("MASAN"));
                    item.setCourtDate(toLocalDateTime(rs.getTimestamp("NGAYTHUE")));
                    item.setUnitPrice(rs.getBigDecimal("DON_GIA_THUE"));
                    item.setStatus(rs.getString("TRANGTHAI"));
                    item.setSportTypeName(rs.getString("SPORT_TYPE_NAME"));

                    int startH = rs.getInt("GIOBATDAU");
                    int endH = rs.getInt("GIOKETTHUC");
                    item.setTimeSlot(String.format("%02d:00 - %02d:00", startH, endH));

                    lastBranch = rs.getString("BRANCH_NAME");
                    lastBranchAddr = rs.getString("BRANCH_ADDRESS");
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy chi tiết sân: " + e.getMessage(), e);
        }

        dto.setCourtItems(items);

        if (lastBranch != null) {
            dto.setBranchName(lastBranch);
            dto.setBranchAddress(lastBranchAddr);
        }

        // Tính trạng thái tổng từ danh sách sân — logic nằm ở DAO, DTO chỉ giữ giá trị
        dto.setOverallStatus(computeOverallStatus(items, dto.getStatus()));

        return dto;
    }

    private static String computeOverallStatus(List<BookingDetailDTO.CourtLineItem> items, String invoiceStatus) {
        if (items == null || items.isEmpty()) {
            return invoiceStatus != null ? invoiceStatus : "TRỐNG";
        }
        boolean hasWaitDeposit = false;
        boolean hasConfirmed   = false;
        boolean allCancelled   = true;
        for (BookingDetailDTO.CourtLineItem item : items) {
            String st = item.getStatus() != null ? item.getStatus().toUpperCase() : "";
            if (!st.contains("HUỶ") && !st.contains("HỦY")) {
                allCancelled = false;
            }
            if (st.contains("CHỜ CỌC") || st.contains("CHƯA")) {
                hasWaitDeposit = true;
            }
            if (st.contains("XÁC NHẬN") || st.contains("ĐÃ CỌC")) {
                hasConfirmed = true;
            }
        }
        if (allCancelled)   return "Đã hủy";
        if (hasWaitDeposit) return "Đã đặt chờ cọc";
        if (hasConfirmed)   return "Đã xác nhận";
        return "TRỐNG";
    }
}