package com.sportcourt.modules.customer_history.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer_history.dto.BookingAddCourtRequest;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.customer_history.dto.PriceBoardOptionDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookingHistoryDAO implements BookingHistoryDAO {

    private static final String SQL_FIND_BY_CUSTOMER = """
            SELECT
                HD.MAHD,
                CT.TRANGTHAI                     AS TRANGTHAI,
                HD.TONGTIEN,
                HD.CREATED_AT,
                LTT.TEN                          AS SPORT_TYPE_NAME,
                CN.TEN_CHI_NHANH                 AS BRANCH_NAME,
                CN.DIACHI                        AS BRANCH_ADDRESS,
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
                    OR UPPER(CT.TRANGTHAI)      LIKE UPPER(?)
              )
            GROUP BY
                HD.MAHD, CT.TRANGTHAI, HD.TONGTIEN, HD.CREATED_AT,
                LTT.TEN, CN.TEN_CHI_NHANH, CN.DIACHI
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

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
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
                ps.setNull(8, Types.VARCHAR);
            } else {
                ps.setString(3, like);
                ps.setString(4, like);
                ps.setString(5, like);
                ps.setString(6, like);
                ps.setString(7, like);
                ps.setString(8, like);
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

        return dto;
    }

    @Override
    public List<String> getAvailableCourtIds() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MASAN FROM SAN_CON WHERE IS_DELETED = 0 AND TRANGTHAI = 'ĐANG HOẠT ĐỘNG'";

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MASAN"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách sân: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<PriceBoardOptionDTO> getAvailablePriceBoards() {
        List<PriceBoardOptionDTO> list = new ArrayList<>();
        String sql = "SELECT MABG, GIOBATDAU, GIOKETTHUC, GIA FROM BANG_GIA WHERE IS_DELETED = 0 ORDER BY GIOBATDAU";

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PriceBoardOptionDTO dto = new PriceBoardOptionDTO();
                dto.setPriceBoardId(rs.getString("MABG"));
                dto.setStartHour(rs.getInt("GIOBATDAU"));
                dto.setEndHour(rs.getInt("GIOKETTHUC"));
                dto.setPrice(rs.getBigDecimal("GIA"));
                list.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách bảng giá: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void addCourtBooking(BookingAddCourtRequest request) {
        try (Connection conn = ConnectionUtils.getMyConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal price = BigDecimal.ZERO;
                try (PreparedStatement psPrice = conn.prepareStatement("SELECT GIA FROM BANG_GIA WHERE MABG = ?")) {
                    psPrice.setString(1, request.getPriceBoardId());
                    try (ResultSet rs = psPrice.executeQuery()) {
                        if (rs.next()) {
                            price = rs.getBigDecimal("GIA");
                        }
                    }
                }

                String detailId = "CTHDTS-" + System.currentTimeMillis();
                String insertSql = "INSERT INTO CHI_TIET_HOA_DON_THUE_SAN (MACT_THUE_SAN, MAHD, MASAN, MABG, NGAYTHUE, DON_GIA_THUE, TRANGTHAI, CREATED_AT, IS_DELETED) " +
                        "VALUES (?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, 'ĐÃ XÁC NHẬN', SYSDATE, 0)";

                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setString(1, detailId);
                    psInsert.setString(2, request.getInvoiceId());
                    psInsert.setString(3, request.getCourtId());
                    psInsert.setString(4, request.getPriceBoardId());
                    psInsert.setString(5, request.getBookingDateStr());
                    psInsert.setBigDecimal(6, price);
                    psInsert.executeUpdate();
                }

                String updateSql = "UPDATE HOA_DON SET TONGTIEN = TONGTIEN + ? WHERE MAHD = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setBigDecimal(1, price);
                    psUpdate.setString(2, request.getInvoiceId());
                    psUpdate.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thêm sân vào hóa đơn: " + e.getMessage(), e);
        }
    }
}