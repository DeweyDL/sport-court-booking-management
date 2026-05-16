package com.sportcourt.modules.customer_booking.service;

import com.sportcourt.modules.customer_booking.dao.CustomerBookingCatalogDAO;
import com.sportcourt.modules.customer_booking.dao.CustomerBookingCatalogDAOImpl;
import com.sportcourt.modules.customer_booking.dao.CustomerBookingOrderDAO;
import com.sportcourt.modules.customer_booking.dao.CustomerBookingOrderDAOImpl;
import com.sportcourt.modules.customer_booking.dao.CustomerBookingScheduleDAO;
import com.sportcourt.modules.customer_booking.dao.CustomerBookingScheduleDAOImpl;
import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchCriteria;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.CreateBookingRequest;
import com.sportcourt.modules.customer_booking.dto.PriceSlot;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;
import com.sportcourt.modules.customer_booking.dto.SportTypeOption;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CustomerBookingServiceImpl implements CustomerBookingService {
    private static final int ORACLE_APPLICATION_ERROR_MIN = 20000;
    private static final int ORACLE_APPLICATION_ERROR_MAX = 20999;

    private final CustomerBookingCatalogDAO catalogDAO;
    private final CustomerBookingScheduleDAO scheduleDAO;
    private final CustomerBookingOrderDAO orderDAO;

    public CustomerBookingServiceImpl() {
        this(new CustomerBookingCatalogDAOImpl(),
                new CustomerBookingScheduleDAOImpl(),
                new CustomerBookingOrderDAOImpl());
    }

    public CustomerBookingServiceImpl(CustomerBookingCatalogDAO catalogDAO,
                                      CustomerBookingScheduleDAO scheduleDAO,
                                      CustomerBookingOrderDAO orderDAO) {
        this.catalogDAO = catalogDAO;
        this.scheduleDAO = scheduleDAO;
        this.orderDAO = orderDAO;
    }

    @Override
    public List<BranchOption> findAvailableBranches() {
        try {
            return catalogDAO.findAvailableBranches();
        } catch (SQLException e) {
            throw databaseError("Không thể tải danh sách chi nhánh.", e);
        }
    }

    @Override
    public List<SportTypeOption> findAvailableSportTypes(String branchId) {
        requireNotBlank(branchId, "Thiếu mã chi nhánh.");
        try {
            return catalogDAO.findAvailableSportTypes(branchId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể tải danh sách loại thể thao.", e);
        }
    }

    @Override
    public List<CourtSearchResult> searchCourts(CourtSearchCriteria criteria) {
        try {
            return catalogDAO.searchCourts(criteria);
        } catch (SQLException e) {
            throw databaseError("Không thể tìm kiếm sân.", e);
        }
    }

    @Override
    public Optional<CourtSearchResult> findCourtDetail(String courtId) {
        requireNotBlank(courtId, "Thiếu mã sân.");
        try {
            return catalogDAO.findCourtDetail(courtId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể tải chi tiết sân.", e);
        }
    }

    @Override
    public List<PriceSlot> findPriceBoardsByArea(String areaId) {
        requireNotBlank(areaId, "Thiếu mã khu vực.");
        try {
            return scheduleDAO.findPriceBoardsByArea(areaId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể tải bảng giá của khu vực.", e);
        }
    }

    @Override
    public List<SlotStatus> findAvailableSlots(String courtId, LocalDate bookingDate) {
        requireNotBlank(courtId, "Thiếu mã sân.");
        requireNotNull(bookingDate, "Thiếu ngày đặt sân.");
        try {
            return scheduleDAO.findAvailableSlots(courtId.trim(), bookingDate);
        } catch (SQLException e) {
            throw databaseError("Không thể tải lịch sân trống.", e);
        }
    }

    @Override
    public List<SlotStatus> findBookedSlots(String areaId, LocalDate bookingDate) {
        requireNotBlank(areaId, "Thiếu mã khu vực.");
        requireNotNull(bookingDate, "Thiếu ngày đặt sân.");
        try {
            return scheduleDAO.findBookedSlots(areaId.trim(), bookingDate);
        } catch (SQLException e) {
            throw databaseError("Không thể tải danh sách khung giờ đã đặt.", e);
        }
    }

    @Override
    public List<String> findCourtsByArea(String areaId) {
        requireNotBlank(areaId, "Thiếu mã khu vực.");
        try {
            return scheduleDAO.findCourtsByArea(areaId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể tải danh sách sân trong khu vực.", e);
        }
    }

    @Override
    public boolean isSlotAvailable(String courtId, String priceBoardId, LocalDate bookingDate) {
        requireNotBlank(courtId, "Thiếu mã sân.");
        requireNotBlank(priceBoardId, "Thiếu mã bảng giá.");
        requireNotNull(bookingDate, "Thiếu ngày đặt sân.");
        try {
            return scheduleDAO.isSlotAvailable(courtId.trim(), priceBoardId.trim(), bookingDate);
        } catch (SQLException e) {
            throw databaseError("Không thể kiểm tra trạng thái khung giờ.", e);
        }
    }

    @Override
    public String findCustomerByUserId(String userId) {
        requireNotBlank(userId, "Thiếu mã người dùng.");
        try {
            return orderDAO.findCustomerByUserId(userId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể tìm thông tin khách hàng.", e);
        }
    }

    @Override
    public Optional<String> findBookingEmployeeByBranch(String branchId) {
        requireNotBlank(branchId, "Thiếu mã chi nhánh.");
        try {
            return orderDAO.findBookingEmployeeByBranch(branchId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể tìm nhân viên xử lý đặt sân.", e);
        }
    }

    @Override
    public BookingPreview createPendingInvoice(CreateBookingRequest request) {
        validateCreateRequest(request);
        try {
            return orderDAO.createPendingInvoice(request);
        } catch (SQLException e) {
            throw databaseError("Không thể tạo hóa đơn đặt sân.", e);
        }
    }

    @Override
    public void insertCourtBookingDetails(String invoiceId, List<SelectedBookingSlot> selectedBookingSlots) {
        requireNotBlank(invoiceId, "Thiếu mã hóa đơn.");
        validateSelectedSlots(selectedBookingSlots);
        try {
            orderDAO.insertCourtBookingDetails(invoiceId.trim(), selectedBookingSlots);
        } catch (SQLException e) {
            throw databaseError("Không thể thêm chi tiết đặt sân vào hóa đơn.", e);
        }
    }

    @Override
    public void cancelPendingBooking(String invoiceId) {
        requireNotBlank(invoiceId, "Thiếu mã hóa đơn.");
        try {
            orderDAO.cancelPendingBooking(invoiceId.trim());
        } catch (SQLException e) {
            throw databaseError("Không thể hủy đặt sân đang chờ thanh toán.", e);
        }
    }

    private RuntimeException databaseError(String fallbackMessage, SQLException e) {
        if (isTriggerError(e)) {
            return new IllegalArgumentException(extractDatabaseMessage(e), e);
        }

        return new RuntimeException(fallbackMessage, e);
    }

    private boolean isTriggerError(SQLException e) {
        SQLException current = e;
        while (current != null) {
            int errorCode = Math.abs(current.getErrorCode());
            if (errorCode >= ORACLE_APPLICATION_ERROR_MIN && errorCode <= ORACLE_APPLICATION_ERROR_MAX) {
                return true;
            }

            String message = current.getMessage();
            if (message != null && message.contains("ORA-20")) {
                return true;
            }

            current = current.getNextException();
        }
        return false;
    }

    private String extractDatabaseMessage(SQLException e) {
        SQLException current = e;
        String fallback = null;
        while (current != null) {
            String message = current.getMessage();
            String extracted = extractOracleApplicationMessage(message);
            if (extracted != null) {
                return extracted;
            }
            if (fallback == null && message != null && !message.isBlank()) {
                fallback = message.trim();
            }

            current = current.getNextException();
        }

        return fallback != null ? fallback : "Dữ liệu đặt sân không hợp lệ.";
    }

    private String extractOracleApplicationMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        String[] lines = message.split("\\R");
        for (String line : lines) {
            if (line.contains("ORA-20")) {
                int colonIndex = line.indexOf(':');
                return colonIndex >= 0 ? line.substring(colonIndex + 1).trim() : line.trim();
            }
        }

        return null;
    }

    private void validateCreateRequest(CreateBookingRequest request) {
        requireNotNull(request, "Thông tin đặt sân không được null.");
        requireNotBlank(request.customerId(), "Thiếu mã khách hàng.");
        requireNotBlank(request.employeeId(), "Thiếu mã nhân viên.");
        validateSelectedSlots(request.selectedSlots());
    }

    private void validateSelectedSlots(List<SelectedBookingSlot> selectedSlots) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một khung giờ đặt sân.");
        }

        for (SelectedBookingSlot slot : selectedSlots) {
            requireNotNull(slot, "Thông tin khung giờ đặt sân không hợp lệ.");
            requireNotBlank(slot.courtId(), "Thiếu mã sân trong khung giờ đặt sân.");
            requireNotBlank(slot.priceBoardId(), "Thiếu mã bảng giá trong khung giờ đặt sân.");
            requireNotNull(slot.bookingDate(), "Thiếu ngày đặt sân trong khung giờ đặt sân.");
        }
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
