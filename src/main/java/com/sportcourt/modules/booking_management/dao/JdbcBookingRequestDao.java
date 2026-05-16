package com.sportcourt.modules.booking_management.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.booking_management.dto.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookingRequestDao implements BookingRequestDao {

    @Override
    public List<BookingBranchOption> findBranchOptions() throws SQLException {
        String sql = """
                SELECT MACN, TEN_CHI_NHANH
                FROM CHI_NHANH
                WHERE IS_DELETED = 0
                ORDER BY TEN_CHI_NHANH ASC, MACN ASC
                """;
        List<BookingBranchOption> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BookingBranchOption(
                        rs.getString("MACN"),
                        rs.getString("TEN_CHI_NHANH")
                ));
            }
        }
        return list;
    }

    @Override
    public List<BookingSportTypeOption> findSportTypeOptions(String branchId) throws SQLException {
        String sql = """
            SELECT DISTINCT ltt.MATT, ltt.TEN
            FROM LOAI_THE_THAO ltt
            JOIN KHU_VUC kv ON ltt.MATT = kv.MATT
            WHERE kv.MACN = ? 
              AND ltt.IS_DELETED = 0 
              AND kv.IS_DELETED = 0
            ORDER BY ltt.TEN ASC
            """;

        List<BookingSportTypeOption> list = new ArrayList<>();

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingSportTypeOption(
                            rs.getString("MATT"),
                            rs.getString("TEN")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<BookingAreaOption> findAreaOptions(String branchId, String sportTypeIdOrNull) throws SQLException {
        String sql = """
                SELECT kv.MAKV,
                       kv.MATT,
                       ltt.TEN AS SPORT_TYPE_NAME
                FROM KHU_VUC kv
                JOIN LOAI_THE_THAO ltt ON ltt.MATT = kv.MATT AND ltt.IS_DELETED = 0
                WHERE kv.IS_DELETED = 0
                  AND kv.MACN = ?
                  AND (? IS NULL OR kv.MATT = ?)
                ORDER BY kv.MAKV ASC
                """;
        List<BookingAreaOption> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branchId);
            if (sportTypeIdOrNull == null || sportTypeIdOrNull.isBlank()) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(2, sportTypeIdOrNull);
                ps.setString(3, sportTypeIdOrNull);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingAreaOption(
                            rs.getString("MAKV"),
                            rs.getString("MATT"),
                            rs.getString("SPORT_TYPE_NAME")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<BookingCourtOption> findCourtsByArea(String areaId) throws SQLException {
        String sql = """
                SELECT MASAN
                FROM SAN_CON
                WHERE IS_DELETED = 0
                  AND MAKV = ?
                  AND TRANGTHAI = 'ĐANG HOẠT ĐỘNG'
                ORDER BY MASAN ASC
                """;
        List<BookingCourtOption> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingCourtOption(rs.getString("MASAN")));
                }
            }
        }
        return list;
    }

    @Override
    public BookingOpenHours getOpenHours() throws SQLException {
        String sql = """
                SELECT MIN(GIOBATDAU) AS MIN_H,
                       MAX(GIOKETTHUC) AS MAX_H
                FROM BANG_GIA
                WHERE IS_DELETED = 0
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int min = rs.getInt("MIN_H");
                int max = rs.getInt("MAX_H");
                if (!rs.wasNull() && min >= 0 && max > min) {
                    return new BookingOpenHours(min, max);
                }
            }
        }
        return BookingOpenHours.defaultHours();
    }

    @Override
    public List<BookingSlotDTO> findBookings(String branchId, String areaId, LocalDate date, String bookingStatus) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT
                ct.MACT_THUE_SAN,
                ct.MAHD,
                ct.MASAN,
                sc.MAKV,
                ltt.TEN AS SPORT_TYPE_NAME,
                u.HOTEN AS CUSTOMER_NAME,
                u.SDT AS CUSTOMER_PHONE,
                bg.GIOBATDAU,
                bg.GIOKETTHUC,
                ct.NGAYTHUE
            FROM CHI_TIET_HOA_DON_THUE_SAN ct
            JOIN HOA_DON hd ON hd.MAHD = ct.MAHD AND hd.IS_DELETED = 0
            JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
            JOIN USERS u ON u.USER_ID = kh.USER_ID
            JOIN BANG_GIA bg ON bg.MABG = ct.MABG AND bg.IS_DELETED = 0
            JOIN SAN_CON sc ON sc.MASAN = ct.MASAN AND sc.IS_DELETED = 0
            JOIN KHU_VUC kv ON kv.MAKV = sc.MAKV AND kv.IS_DELETED = 0
            JOIN LOAI_THE_THAO ltt ON ltt.MATT = kv.MATT AND ltt.IS_DELETED = 0
            WHERE ct.IS_DELETED = 0
              AND kv.MACN = ?
              AND kv.MAKV = ?
              AND TRUNC(ct.NGAYTHUE) = ?
            """);

        if (bookingStatus != null && !bookingStatus.isBlank()) {
            sqlBuilder.append(" AND ct.TRANGTHAI = ?");
        }

        sqlBuilder.append(" ORDER BY ct.MASAN, bg.GIOBATDAU");

        List<BookingSlotDTO> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            ps.setString(paramIndex++, branchId);
            ps.setString(paramIndex++, areaId);
            ps.setObject(paramIndex++, date);

            if (bookingStatus != null && !bookingStatus.isBlank()) {
                ps.setString(paramIndex++, bookingStatus);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date ngayThueSql = rs.getDate("NGAYTHUE");
                    LocalDate bookingDate = (ngayThueSql != null) ? ngayThueSql.toLocalDate() : null;

                    list.add(new BookingSlotDTO(
                            rs.getString("MACT_THUE_SAN"),
                            rs.getString("MAHD"),
                            rs.getString("MASAN"),
                            rs.getString("MAKV"),
                            rs.getString("SPORT_TYPE_NAME"),
                            rs.getString("CUSTOMER_NAME"),
                            rs.getString("CUSTOMER_PHONE"),
                            rs.getInt("GIOBATDAU"),
                            rs.getInt("GIOKETTHUC"),
                            bookingDate
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public BookingInvoiceDTO getInvoiceDetails(String bookingDetailId) throws SQLException {
        BookingInvoiceDTO invoiceDetails = null;

        // TỐI ƯU: Sử dụng JOIN trực tiếp thay vì Subquery để tránh lỗi quét dữ liệu trên Oracle
        String sqlHeader = """
            SELECT 
                hd.MAHD,
                u.HOTEN AS CUSTOMER_NAME,
                u.SDT AS CUSTOMER_PHONE,
                cn.TEN_CHI_NHANH,
                cn.DIACHI AS BRANCH_ADDRESS,
                SUM(bg.GIA) AS TONG_TIEN_SAN,
                ct.TRANGTHAI AS DETAIL_STATUS
            FROM CHI_TIET_HOA_DON_THUE_SAN ct
            JOIN HOA_DON hd ON hd.MAHD = ct.MAHD AND hd.IS_DELETED = 0
            JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
            JOIN USERS u ON u.USER_ID = kh.USER_ID
            JOIN BANG_GIA bg ON bg.MABG = ct.MABG AND bg.IS_DELETED = 0
            JOIN SAN_CON sc ON sc.MASAN = ct.MASAN AND sc.IS_DELETED = 0
            JOIN KHU_VUC kv ON kv.MAKV = sc.MAKV AND kv.IS_DELETED = 0
            JOIN CHI_NHANH cn ON cn.MACN = kv.MACN AND cn.IS_DELETED = 0
            WHERE ct.MACT_THUE_SAN = ? AND ct.IS_DELETED = 0
            GROUP BY hd.MAHD, u.HOTEN, u.SDT, cn.TEN_CHI_NHANH, cn.DIACHI, ct.TRANGTHAI
            """;

        // TỐI ƯU: Tìm danh sách toàn bộ các sân chung Hóa đơn bằng cách JOIN chính nó qua MAHD
        String sqlPitches = """
            SELECT
                target.MASAN,
                bg.GIOBATDAU,
                bg.GIOKETTHUC,
                target.NGAYTHUE,
                bg.GIA
            FROM CHI_TIET_HOA_DON_THUE_SAN current_slot
            JOIN CHI_TIET_HOA_DON_THUE_SAN target ON target.MAHD = current_slot.MAHD AND target.IS_DELETED = 0
            JOIN BANG_GIA bg ON bg.MABG = target.MABG AND bg.IS_DELETED = 0
            WHERE current_slot.MACT_THUE_SAN = ? AND current_slot.IS_DELETED = 0
            ORDER BY target.NGAYTHUE ASC, bg.GIOBATDAU ASC
            """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            String customerName = "", phone = "", branchName = "", branchAddress = "", status = "", realInvoiceId = "";
            double totalAmount = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, bookingDetailId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        realInvoiceId = rs.getString("MAHD");
                        customerName = rs.getString("CUSTOMER_NAME");
                        phone = rs.getString("CUSTOMER_PHONE");
                        branchName = rs.getString("TEN_CHI_NHANH");
                        branchAddress = rs.getString("BRANCH_ADDRESS");
                        totalAmount = rs.getDouble("TONG_TIEN_SAN");
                        status = rs.getString("DETAIL_STATUS");
                    } else {
                        return null;
                    }
                }
            }

            List<BookingPitchDetailDTO> pitches = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlPitches)) {
                ps.setString(1, bookingDetailId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.sql.Date ngayThueSql = rs.getDate("NGAYTHUE");
                        LocalDate pDate = (ngayThueSql != null) ? ngayThueSql.toLocalDate() : null;

                        pitches.add(new BookingPitchDetailDTO(
                                rs.getString("MASAN"),
                                rs.getInt("GIOBATDAU"),
                                rs.getInt("GIOKETTHUC"),
                                pDate,
                                rs.getDouble("GIA")
                        ));
                    }
                }
            }

            invoiceDetails = new BookingInvoiceDTO(
                    realInvoiceId, customerName, phone, branchName, branchAddress, totalAmount, status, pitches
            );
        }

        return invoiceDetails;
    }

    @Override
    public boolean cancelBookingInvoice(String bookingDetailId) throws SQLException {
        // SỬA: Gọi trực tiếp Stored Procedure của Oracle để xử lý nghiệp vụ hoàn cọc/hủy hóa đơn
        String sql = "{CALL PRC_HUY_CHI_TIET_THUE_SAN(?)}";

        try (Connection conn = ConnectionUtils.getMyConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            cstmt.setString(1, bookingDetailId);

            // Thực thi Procedure
            cstmt.execute();

            // Vì Procedure tự quăng lỗi (RAISE_APPLICATION_ERROR) nếu thất bại,
            // nên nếu chạy đến dòng này tức là đã xử lý hoàn tất thành công trong DB.
            return true;
        } catch (SQLException e) {
            // In chi tiết mã lỗi nghiệp vụ Oracle (ví dụ: -20160, -20161) ra để debug nếu cần
            System.err.println("Lỗi nghiệp vụ Oracle khi hủy đặt sân: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<BookingSlotDTO> findBookingsByInvoiceId(String invoiceId) throws SQLException {
        String sql = """
                SELECT
                    ct.MACT_THUE_SAN,
                    ct.MAHD,
                    ct.MASAN,
                    sc.MAKV,
                    ltt.TEN AS SPORT_TYPE_NAME,
                    u.HOTEN AS CUSTOMER_NAME,
                    u.SDT AS CUSTOMER_PHONE,
                    bg.GIOBATDAU,
                    bg.GIOKETTHUC,
                    ct.NGAYTHUE
                FROM CHI_TIET_HOA_DON_THUE_SAN ct
                JOIN HOA_DON hd ON hd.MAHD = ct.MAHD AND hd.IS_DELETED = 0
                JOIN KHACH_HANG kh ON kh.MAKH = hd.MAKH
                JOIN USERS u ON u.USER_ID = kh.USER_ID
                JOIN BANG_GIA bg ON bg.MABG = ct.MABG AND bg.IS_DELETED = 0
                JOIN SAN_CON sc ON sc.MASAN = ct.MASAN AND sc.IS_DELETED = 0
                JOIN KHU_VUC kv ON kv.MAKV = sc.MAKV AND kv.IS_DELETED = 0
                JOIN LOAI_THE_THAO ltt ON ltt.MATT = kv.MATT AND ltt.IS_DELETED = 0
                WHERE ct.IS_DELETED = 0
                  AND ct.MAHD = ?
                ORDER BY ct.MASAN, bg.GIOBATDAU
                """;
        List<BookingSlotDTO> list = new ArrayList<>();
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date ngayThueSql = rs.getDate("NGAYTHUE");
                    LocalDate bookingDate = (ngayThueSql != null) ? ngayThueSql.toLocalDate() : null;

                    list.add(new BookingSlotDTO(
                            rs.getString("MACT_THUE_SAN"),
                            rs.getString("MAHD"),
                            rs.getString("MASAN"),
                            rs.getString("MAKV"),
                            rs.getString("SPORT_TYPE_NAME"),
                            rs.getString("CUSTOMER_NAME"),
                            rs.getString("CUSTOMER_PHONE"),
                            rs.getInt("GIOBATDAU"),
                            rs.getInt("GIOKETTHUC"),
                            bookingDate
                    ));
                }
            }
        }
        return list;
    }
}