package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.customer_booking.dto.BookingCourtLine;
import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.CreateBookingRequest;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerBookingOrderDAOImpl implements CustomerBookingOrderDAO {
    private static final String BOOKING_DETAIL_STATUS_WAITING_DEPOSIT = "ĐÃ ĐẶT CHỜ CỌC";
    private static final String BOOKING_DETAIL_STATUS_DEPOSITED = "ĐÃ CỌC";
    private static final String BOOKING_DETAIL_STATUS_CANCELLED = "ĐÃ HUỶ";
    private static final String INVOICE_STATUS_UNPAID = "CHƯA THANH TOÁN";
    private static final String INVOICE_STATUS_CANCELLED = "ĐÃ HUỶ";

    @Override
    public String findCustomerByUserId(String userId) throws SQLException {
        String sql = """
                SELECT MAKH
                FROM KHACH_HANG
                WHERE USER_ID = ?
                    AND TRANGTHAI = 'ACTIVE'
                    AND IS_DELETED = 0
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MAKH") : null;
            }
        }
    }

    @Override
    public Optional<String> findBookingEmployeeByBranch(String branchId) throws SQLException {
        String sql = """
                SELECT MANV
                FROM (
                    SELECT MANV
                    FROM NHAN_VIEN
                    WHERE MACN = ?
                        AND IS_DELETED = 0
                        AND UPPER(TRANG_THAI) = 'ACTIVE'
                    ORDER BY CASE WHEN IS_QL = 0 THEN 0 ELSE 1 END, MANV
                )
                WHERE ROWNUM = 1
                """;

        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.ofNullable(rs.getString("MANV"));
            }
        }
    }

    @Override
    public BookingPreview createPendingInvoice(CreateBookingRequest request) throws SQLException {
        validateCreateRequest(request);

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String invoiceId = generateNextInvoiceId(connection);
                insertCourtBookingDetails(
                        connection,
                        invoiceId,
                        request.customerId(),
                        request.employeeId(),
                        request.selectedSlots(),
                        isAdvanceBooking(request),
                        normalizeMoney(request.discount())
                );

                BookingPreview preview = findBookingPreview(connection, invoiceId)
                        .orElseThrow(() -> new SQLException("Không tìm thấy hóa đơn vừa tạo: " + invoiceId));
                connection.commit();
                return preview;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public void insertCourtBookingDetails(String invoiceId, List<SelectedBookingSlot> selectedBookingSlot)
            throws SQLException {
        if (selectedBookingSlot == null || selectedBookingSlot.isEmpty()) {
            return;
        }

        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                InvoiceContext invoice = findInvoiceContext(connection, invoiceId)
                        .orElseThrow(() -> new SQLException("Hóa đơn không tồn tại hoặc đã bị xóa: " + invoiceId));
                insertCourtBookingDetails(
                        connection,
                        invoiceId,
                        invoice.customerId(),
                        invoice.employeeId(),
                        selectedBookingSlot,
                        invoice.advanceBooking(),
                        invoice.discount()
                );
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public void cancelPendingBooking(String invoiceId) throws SQLException {
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                List<String> detailIds = findActiveBookingDetailIds(connection, invoiceId);
                if (detailIds.isEmpty()) {
                    cancelEmptyPendingInvoice(connection, invoiceId);
                } else {
                    for (String detailId : detailIds) {
                        cancelBookingDetail(connection, detailId);
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void insertCourtBookingDetails(Connection connection,
                                           String invoiceId,
                                           String customerId,
                                           String employeeId,
                                           List<SelectedBookingSlot> selectedSlots,
                                           boolean advanceBooking,
                                           BigDecimal discount) throws SQLException {
        validateSelectedSlots(selectedSlots);
        int nextDetailNumber = findNextNumericId(
                connection,
                "CHI_TIET_HOA_DON_THUE_SAN",
                "MACT_THUE_SAN",
                "CTHDTS-"
        );

        for (SelectedBookingSlot slot : selectedSlots) {
            String detailId = "CTHDTS-" + nextDetailNumber++;
            callBookCourtProcedure(
                    connection,
                    invoiceId,
                    detailId,
                    customerId,
                    employeeId,
                    slot,
                    advanceBooking,
                    discount
            );
            if (advanceBooking) {
                markBookingDetailWaitingDeposit(connection, detailId);
            }
        }
    }

    private void callBookCourtProcedure(Connection connection,
                                        String invoiceId,
                                        String detailId,
                                        String customerId,
                                        String employeeId,
                                        SelectedBookingSlot slot,
                                        boolean advanceBooking,
                                        BigDecimal discount) throws SQLException {
        String sql = "{call PRC_DAT_SAN(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, invoiceId);
            cs.setString(2, detailId);
            cs.setString(3, customerId);
            cs.setString(4, employeeId);
            cs.setString(5, slot.courtId());
            cs.setString(6, slot.priceBoardId());
            cs.setDate(7, Date.valueOf(slot.bookingDate()));
            cs.setInt(8, advanceBooking ? 1 : 0);
            cs.setBigDecimal(9, discount);
            cs.execute();
        }
    }

    private void markBookingDetailDeposited(Connection connection, String detailId) throws SQLException {
        String sql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN
                SET TRANGTHAI = ?
                WHERE MACT_THUE_SAN = ?
                    AND IS_DELETED = 0
                    AND TRANGTHAI = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, BOOKING_DETAIL_STATUS_DEPOSITED);
            ps.setString(2, detailId);
            ps.setString(3, BOOKING_DETAIL_STATUS_WAITING_DEPOSIT);
            ps.executeUpdate();
        }
    }

    private void markBookingDetailWaitingDeposit(Connection connection, String detailId) throws SQLException {
        String sql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN
                SET TRANGTHAI = ?
                WHERE MACT_THUE_SAN = ?
                    AND IS_DELETED = 0
                    AND TRANGTHAI <> ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, BOOKING_DETAIL_STATUS_WAITING_DEPOSIT);
            ps.setString(2, detailId);
            ps.setString(3, BOOKING_DETAIL_STATUS_CANCELLED);
            ps.executeUpdate();
        }
    }

    private void cancelBookingDetail(Connection connection, String detailId) throws SQLException {
        String sql = "{call PRC_HUY_CHI_TIET_THUE_SAN(?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, detailId);
            cs.execute();
        }
    }

    private List<String> findActiveBookingDetailIds(Connection connection, String invoiceId) throws SQLException {
        String sql = """
                SELECT MACT_THUE_SAN
                FROM CHI_TIET_HOA_DON_THUE_SAN
                WHERE MAHD = ?
                    AND IS_DELETED = 0
                    AND TRANGTHAI <> ?
                ORDER BY NGAYTHUE, MACT_THUE_SAN
                """;

        List<String> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, invoiceId);
            ps.setString(2, BOOKING_DETAIL_STATUS_CANCELLED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MACT_THUE_SAN"));
                }
            }
        }
        return list;
    }

    private void cancelEmptyPendingInvoice(Connection connection, String invoiceId) throws SQLException {
        String sql = """
                UPDATE HOA_DON HD
                SET HD.TRANGTHAI = ?
                WHERE HD.MAHD = ?
                    AND HD.TRANGTHAI = ?
                    AND HD.IS_DELETED = 0
                    AND NOT EXISTS (
                        SELECT 1
                        FROM CHI_TIET_HOA_DON_THUE_SAN CT
                        WHERE CT.MAHD = HD.MAHD
                            AND CT.IS_DELETED = 0
                            AND CT.TRANGTHAI <> ?
                    )
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, INVOICE_STATUS_CANCELLED);
            ps.setString(2, invoiceId);
            ps.setString(3, INVOICE_STATUS_UNPAID);
            ps.setString(4, BOOKING_DETAIL_STATUS_CANCELLED);
            ps.executeUpdate();
        }
    }

    private Optional<InvoiceContext> findInvoiceContext(Connection connection, String invoiceId) throws SQLException {
        String sql = """
                SELECT
                    HD.MAKH,
                    HD.MANV,
                    HD.GIAMGIA,
                    HD.TIEN_COC
                FROM HOA_DON HD
                WHERE HD.MAHD = ?
                    AND HD.TRANGTHAI = ?
                    AND HD.IS_DELETED = 0
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, invoiceId);
            ps.setString(2, INVOICE_STATUS_UNPAID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                BigDecimal deposit = normalizeMoney(rs.getBigDecimal("TIEN_COC"));
                boolean advanceBooking = deposit.compareTo(BigDecimal.ZERO) > 0;

                return Optional.of(new InvoiceContext(
                        rs.getString("MAKH"),
                        rs.getString("MANV"),
                        normalizeMoney(rs.getBigDecimal("GIAMGIA")),
                        advanceBooking
                ));
            }
        }
    }

    private Optional<BookingPreview> findBookingPreview(Connection connection, String invoiceId) throws SQLException {
        String headerSql = """
                SELECT
                    HD.MAHD,
                    HD.MAKH,
                    U.HOTEN AS CUSTOMER_NAME,
                    U.SDT AS PHONE_NUMBER,
                    NV.MACN AS STAFF_BRANCH_ID,
                    CN.TEN_CHI_NHANH AS STAFF_BRANCH_NAME,
                    CN.DIACHI AS STAFF_BRANCH_ADDRESS,
                    HD.TIEN_COC,
                    HD.GIAMGIA,
                    HD.TONGGIATRI,
                    HD.TONGTIEN,
                    HD.TRANGTHAI
                FROM HOA_DON HD
                JOIN KHACH_HANG KH
                    ON KH.MAKH = HD.MAKH
                    AND KH.IS_DELETED = 0
                JOIN USERS U
                    ON U.USER_ID = KH.USER_ID
                    AND U.IS_DELETED = 0
                JOIN NHAN_VIEN NV
                    ON NV.MANV = HD.MANV
                    AND NV.IS_DELETED = 0
                JOIN CHI_NHANH CN
                    ON CN.MACN = NV.MACN
                    AND CN.IS_DELETED = 0
                WHERE HD.MAHD = ?
                    AND HD.IS_DELETED = 0
                """;

        String lineSql = """
                SELECT
                    CT.MASAN,
                    CT.NGAYTHUE,
                    CT.DON_GIA_THUE,
                    CT.TRANGTHAI,
                    BG.GIOBATDAU,
                    BG.GIOKETTHUC,
                    LTT.TEN AS SPORT_TYPE_NAME,
                    CN.MACN AS BRANCH_ID,
                    CN.TEN_CHI_NHANH AS BRANCH_NAME,
                    CN.DIACHI AS BRANCH_ADDRESS
                FROM CHI_TIET_HOA_DON_THUE_SAN CT
                JOIN BANG_GIA BG
                    ON BG.MABG = CT.MABG
                    AND BG.IS_DELETED = 0
                JOIN SAN_CON SC
                    ON SC.MASAN = CT.MASAN
                    AND SC.IS_DELETED = 0
                JOIN KHU_VUC KV
                    ON KV.MAKV = SC.MAKV
                    AND KV.IS_DELETED = 0
                JOIN LOAI_THE_THAO LTT
                    ON LTT.MATT = KV.MATT
                    AND LTT.IS_DELETED = 0
                JOIN CHI_NHANH CN
                    ON CN.MACN = KV.MACN
                    AND CN.IS_DELETED = 0
                WHERE CT.MAHD = ?
                    AND CT.IS_DELETED = 0
                    AND CT.TRANGTHAI <> ?
                ORDER BY CT.NGAYTHUE, BG.GIOBATDAU, CT.MASAN
                """;

        BookingHeader header;
        try (PreparedStatement ps = connection.prepareStatement(headerSql)) {
            ps.setString(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                header = new BookingHeader(
                        rs.getString("MAHD"),
                        rs.getString("MAKH"),
                        rs.getString("CUSTOMER_NAME"),
                        rs.getString("PHONE_NUMBER"),
                        rs.getString("STAFF_BRANCH_ID"),
                        rs.getString("STAFF_BRANCH_NAME"),
                        rs.getString("STAFF_BRANCH_ADDRESS"),
                        normalizeMoney(rs.getBigDecimal("TIEN_COC")),
                        normalizeMoney(rs.getBigDecimal("GIAMGIA")),
                        normalizeMoney(rs.getBigDecimal("TONGGIATRI")),
                        normalizeMoney(rs.getBigDecimal("TONGTIEN")),
                        rs.getString("TRANGTHAI")
                );
            }
        }

        List<BookingCourtLine> courtLines = new ArrayList<>();
        BigDecimal totalCourtPrice = BigDecimal.ZERO;
        String branchId = header.branchId();
        String branchName = header.branchName();
        String branchAddress = header.branchAddress();

        try (PreparedStatement ps = connection.prepareStatement(lineSql)) {
            ps.setString(1, invoiceId);
            ps.setString(2, BOOKING_DETAIL_STATUS_CANCELLED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int startHour = rs.getInt("GIOBATDAU");
                    int endHour = rs.getInt("GIOKETTHUC");
                    BigDecimal unitPrice = normalizeMoney(rs.getBigDecimal("DON_GIA_THUE"));
                    if (courtLines.isEmpty()) {
                        branchId = rs.getString("BRANCH_ID");
                        branchName = rs.getString("BRANCH_NAME");
                        branchAddress = rs.getString("BRANCH_ADDRESS");
                    }

                    courtLines.add(new BookingCourtLine(
                            rs.getString("MASAN"),
                            rs.getString("SPORT_TYPE_NAME"),
                            rs.getDate("NGAYTHUE").toLocalDate().atTime(startHour, 0),
                            formatHour(startHour),
                            formatHour(endHour),
                            unitPrice,
                            rs.getString("TRANGTHAI")
                    ));
                    totalCourtPrice = totalCourtPrice.add(unitPrice);
                }
            }
        }

        return Optional.of(new BookingPreview(
                header.invoiceId(),
                header.customerId(),
                header.customerName(),
                header.phoneNumber(),
                branchId,
                branchName,
                branchAddress,
                courtLines,
                totalCourtPrice,
                header.deposit(),
                header.discount(),
                header.totalAmount(),
                header.invoiceStatus()
        ));
    }

    private String generateNextInvoiceId(Connection connection) throws SQLException {
        return "HD-" + findNextNumericId(connection, "HOA_DON", "MAHD", "HD-");
    }

    private int findNextNumericId(Connection connection,
                                  String tableName,
                                  String columnName,
                                  String prefix) throws SQLException {
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
    public void markAllDetailsDeposited(String invoiceId) throws SQLException {
        String sql = """
                UPDATE CHI_TIET_HOA_DON_THUE_SAN
                SET TRANGTHAI = ?
                WHERE MAHD = ?
                    AND IS_DELETED = 0
                    AND TRANGTHAI = ?
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, BOOKING_DETAIL_STATUS_DEPOSITED);
            ps.setString(2, invoiceId);
            ps.setString(3, BOOKING_DETAIL_STATUS_WAITING_DEPOSIT);
            ps.executeUpdate();
        }
    }

    private boolean isAdvanceBooking(CreateBookingRequest request) {
        return request.deposit() != null && request.deposit().compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatHour(int hour) {
        return String.format("%02d:00", hour);
    }

    private void validateCreateRequest(CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateBookingRequest không được null.");
        }
        if (isBlank(request.customerId())) {
            throw new IllegalArgumentException("customerId không được trống.");
        }
        if (isBlank(request.employeeId())) {
            throw new IllegalArgumentException("employeeId không được trống.");
        }
        validateSelectedSlots(request.selectedSlots());
    }

    private void validateSelectedSlots(List<SelectedBookingSlot> selectedSlots) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một khung giờ đặt sân.");
        }
        for (SelectedBookingSlot slot : selectedSlots) {
            if (slot == null
                    || isBlank(slot.courtId())
                    || isBlank(slot.priceBoardId())
                    || slot.bookingDate() == null) {
                throw new IllegalArgumentException("Thông tin khung giờ đặt sân không hợp lệ.");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record InvoiceContext(
            String customerId,
            String employeeId,
            BigDecimal discount,
            boolean advanceBooking
    ) {
    }

    private record BookingHeader(
            String invoiceId,
            String customerId,
            String customerName,
            String phoneNumber,
            String branchId,
            String branchName,
            String branchAddress,
            BigDecimal deposit,
            BigDecimal discount,
            BigDecimal totalValue,
            BigDecimal totalAmount,
            String invoiceStatus
    ) {
    }
}
