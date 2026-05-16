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
            throw databaseError("Khong the tai danh sach chi nhanh.", e);
        }
    }

    @Override
    public List<SportTypeOption> findAvailableSportTypes(String branchId) {
        requireNotBlank(branchId, "Thieu ma chi nhanh.");
        try {
            return catalogDAO.findAvailableSportTypes(branchId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the tai danh sach loai the thao.", e);
        }
    }

    @Override
    public List<CourtSearchResult> searchCourts(CourtSearchCriteria criteria) {
        try {
            return catalogDAO.searchCourts(criteria);
        } catch (SQLException e) {
            throw databaseError("Khong the tim kiem san.", e);
        }
    }

    @Override
    public Optional<CourtSearchResult> findCourtDetail(String courtId) {
        requireNotBlank(courtId, "Thieu ma san.");
        try {
            return catalogDAO.findCourtDetail(courtId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the tai chi tiet san.", e);
        }
    }

    @Override
    public List<PriceSlot> findPriceBoardsByArea(String areaId) {
        requireNotBlank(areaId, "Thieu ma khu vuc.");
        try {
            return scheduleDAO.findPriceBoardsByArea(areaId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the tai bang gia cua khu vuc.", e);
        }
    }

    @Override
    public List<SlotStatus> findAvailableSlots(String courtId, LocalDate bookingDate) {
        requireNotBlank(courtId, "Thieu ma san.");
        requireNotNull(bookingDate, "Thieu ngay dat san.");
        try {
            return scheduleDAO.findAvailableSlots(courtId.trim(), bookingDate);
        } catch (SQLException e) {
            throw databaseError("Khong the tai lich san trong.", e);
        }
    }

    @Override
    public List<SlotStatus> findBookedSlots(String areaId, LocalDate bookingDate) {
        requireNotBlank(areaId, "Thieu ma khu vuc.");
        requireNotNull(bookingDate, "Thieu ngay dat san.");
        try {
            return scheduleDAO.findBookedSlots(areaId.trim(), bookingDate);
        } catch (SQLException e) {
            throw databaseError("Khong the tai danh sach khung gio da dat.", e);
        }
    }

    @Override
    public List<String> findCourtsByArea(String areaId) {
        requireNotBlank(areaId, "Thieu ma khu vuc.");
        try {
            return scheduleDAO.findCourtsByArea(areaId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the tai danh sach san trong khu vuc.", e);
        }
    }

    @Override
    public boolean isSlotAvailable(String courtId, String priceBoardId, LocalDate bookingDate) {
        requireNotBlank(courtId, "Thieu ma san.");
        requireNotBlank(priceBoardId, "Thieu ma bang gia.");
        requireNotNull(bookingDate, "Thieu ngay dat san.");
        try {
            return scheduleDAO.isSlotAvailable(courtId.trim(), priceBoardId.trim(), bookingDate);
        } catch (SQLException e) {
            throw databaseError("Khong the kiem tra trang thai khung gio.", e);
        }
    }

    @Override
    public String findCustomerByUserId(String userId) {
        requireNotBlank(userId, "Thieu ma nguoi dung.");
        try {
            return orderDAO.findCustomerByUserId(userId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the tim thong tin khach hang.", e);
        }
    }

    @Override
    public Optional<String> findBookingEmployeeByBranch(String branchId) {
        requireNotBlank(branchId, "Thieu ma chi nhanh.");
        try {
            return orderDAO.findBookingEmployeeByBranch(branchId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the tim nhan vien xu ly dat san.", e);
        }
    }

    @Override
    public BookingPreview createPendingInvoice(CreateBookingRequest request) {
        validateCreateRequest(request);
        try {
            return orderDAO.createPendingInvoice(request);
        } catch (SQLException e) {
            throw databaseError("Khong the tao hoa don dat san.", e);
        }
    }

    @Override
    public void insertCourtBookingDetails(String invoiceId, List<SelectedBookingSlot> selectedBookingSlots) {
        requireNotBlank(invoiceId, "Thieu ma hoa don.");
        validateSelectedSlots(selectedBookingSlots);
        try {
            orderDAO.insertCourtBookingDetails(invoiceId.trim(), selectedBookingSlots);
        } catch (SQLException e) {
            throw databaseError("Khong the them chi tiet dat san vao hoa don.", e);
        }
    }

    @Override
    public void cancelPendingBooking(String invoiceId) {
        requireNotBlank(invoiceId, "Thieu ma hoa don.");
        try {
            orderDAO.cancelPendingBooking(invoiceId.trim());
        } catch (SQLException e) {
            throw databaseError("Khong the huy booking dang cho thanh toan.", e);
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

        return fallback != null ? fallback : "Du lieu dat san khong hop le.";
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
        requireNotNull(request, "Thong tin dat san khong duoc null.");
        requireNotBlank(request.customerId(), "Thieu ma khach hang.");
        requireNotBlank(request.employeeId(), "Thieu ma nhan vien.");
        validateSelectedSlots(request.selectedSlots());
    }

    private void validateSelectedSlots(List<SelectedBookingSlot> selectedSlots) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new IllegalArgumentException("Phai chon it nhat mot khung gio dat san.");
        }

        for (SelectedBookingSlot slot : selectedSlots) {
            requireNotNull(slot, "Thong tin khung gio dat san khong hop le.");
            requireNotBlank(slot.courtId(), "Thieu ma san trong khung gio dat san.");
            requireNotBlank(slot.priceBoardId(), "Thieu ma bang gia trong khung gio dat san.");
            requireNotNull(slot.bookingDate(), "Thieu ngay dat san trong khung gio dat san.");
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
