package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer_booking.dto.BookingSlotStatus;
import com.sportcourt.modules.customer_booking.dto.PriceSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerBookingScheduleDAOImpl implements CustomerBookingScheduleDAO {
    private static final String COURT_STATUS_ACTIVE = "\u0110ANG HO\u1EA0T \u0110\u1ED8NG";
    private static final String BOOKING_DETAIL_STATUS_CANCELLED = "\u0110\u00C3 HU\u1EF6";

    @Override
    public List<PriceSlot> findPriceBoardsByArea(String areaId) throws SQLException {
        String sql = """
                SELECT MABG, MAKV, GIOBATDAU, GIOKETTHUC, GIA
                FROM BANG_GIA
                WHERE MAKV = ?
                    AND IS_DELETED = 0
                ORDER BY GIOBATDAU, MABG
                """;

        List<PriceSlot> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PriceSlot(
                            rs.getString("MABG"),
                            rs.getString("MAKV"),
                            rs.getInt("GIOBATDAU"),
                            rs.getInt("GIOKETTHUC"),
                            rs.getBigDecimal("GIA")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<SlotStatus> findAvailableSlots(String courtId, LocalDate bookingDate) throws SQLException {
        String sql = """
                SELECT
                    SC.MASAN AS COURT_ID,
                    BG.MABG AS PRICE_BOARD_ID,
                    BG.GIOBATDAU,
                    BG.GIOKETTHUC,
                    BG.GIA,
                    CASE
                        WHEN SC.TRANGTHAI <> ? THEN 'MAINTENANCE'
                        WHEN FN_KIEM_TRA_SAN_TRONG(SC.MASAN, ?, BG.MABG) = 1 THEN 'AVAILABLE'
                        ELSE 'BOOKED'
                    END AS SLOT_STATUS
                FROM SAN_CON SC
                JOIN KHU_VUC KV
                    ON KV.MAKV = SC.MAKV
                    AND KV.IS_DELETED = 0
                JOIN BANG_GIA BG
                    ON BG.MAKV = KV.MAKV
                    AND BG.IS_DELETED = 0
                WHERE SC.MASAN = ?
                    AND SC.IS_DELETED = 0
                ORDER BY BG.GIOBATDAU, BG.MABG
                """;

        List<SlotStatus> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, COURT_STATUS_ACTIVE);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setString(3, courtId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSlotStatus(rs, bookingDate));
                }
            }
        }
        return list;
    }

    @Override
    public List<SlotStatus> findBookedSlots(String areaId, LocalDate bookingDate) throws SQLException {
        String sql = """
                SELECT
                    CT.MASAN AS COURT_ID,
                    BG.MABG AS PRICE_BOARD_ID,
                    BG.GIOBATDAU,
                    BG.GIOKETTHUC,
                    CT.DON_GIA_THUE AS GIA,
                    'BOOKED' AS SLOT_STATUS
                FROM CHI_TIET_HOA_DON_THUE_SAN CT
                JOIN SAN_CON SC
                    ON SC.MASAN = CT.MASAN
                    AND SC.IS_DELETED = 0
                JOIN KHU_VUC KV
                    ON KV.MAKV = SC.MAKV
                    AND KV.IS_DELETED = 0
                JOIN BANG_GIA BG
                    ON BG.MABG = CT.MABG
                    AND BG.IS_DELETED = 0
                WHERE KV.MAKV = ?
                    AND TRUNC(CT.NGAYTHUE) = TRUNC(?)
                    AND CT.IS_DELETED = 0
                    AND CT.TRANGTHAI <> ?
                ORDER BY CT.MASAN, BG.GIOBATDAU, BG.MABG
                """;

        List<SlotStatus> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, areaId);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setString(3, BOOKING_DETAIL_STATUS_CANCELLED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSlotStatus(rs, bookingDate));
                }
            }
        }
        return list;
    }

    @Override
    public List<String> findCourtsByArea(String areaId) throws SQLException {
        String sql = """
                SELECT MASAN
                FROM SAN_CON
                WHERE MAKV = ?
                    AND IS_DELETED = 0
                ORDER BY MASAN
                """;

        List<String> list = new ArrayList<>();
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MASAN"));
                }
            }
        }
        return list;
    }

    @Override
    public boolean isSlotAvailable(String courtId, String priceBoardId, LocalDate bookingDate) throws SQLException {
        String sql = """
                SELECT
                    CASE
                        WHEN SC.TRANGTHAI = ?
                            AND FN_KIEM_TRA_SAN_TRONG(SC.MASAN, ?, BG.MABG) = 1
                        THEN 1
                        ELSE 0
                    END AS IS_AVAILABLE
                FROM SAN_CON SC
                JOIN BANG_GIA BG
                    ON BG.MAKV = SC.MAKV
                    AND BG.IS_DELETED = 0
                WHERE SC.MASAN = ?
                    AND BG.MABG = ?
                    AND SC.IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, COURT_STATUS_ACTIVE);
            ps.setDate(2, Date.valueOf(bookingDate));
            ps.setString(3, courtId);
            ps.setString(4, priceBoardId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("IS_AVAILABLE") == 1;
            }
        }
    }

    private SlotStatus mapSlotStatus(ResultSet rs, LocalDate bookingDate) throws SQLException {
        return new SlotStatus(
                rs.getString("COURT_ID"),
                rs.getString("PRICE_BOARD_ID"),
                bookingDate,
                rs.getInt("GIOBATDAU"),
                rs.getInt("GIOKETTHUC"),
                rs.getBigDecimal("GIA"),
                BookingSlotStatus.valueOf(rs.getString("SLOT_STATUS"))
        );
    }
}
