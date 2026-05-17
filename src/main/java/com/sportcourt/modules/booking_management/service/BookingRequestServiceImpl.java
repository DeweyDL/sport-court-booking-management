package com.sportcourt.modules.booking_management.service;

import com.sportcourt.modules.booking_management.dao.BookingRequestDao;
import com.sportcourt.modules.booking_management.dao.JdbcBookingRequestDao;
import com.sportcourt.modules.booking_management.dto.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingRequestServiceImpl implements BookingRequestService {
    private final BookingRequestDao dao;

    public BookingRequestServiceImpl() {
        this(new JdbcBookingRequestDao());
    }

    public BookingRequestServiceImpl(BookingRequestDao dao) {
        this.dao = dao;
    }

    @Override
    public List<BookingBranchOption> getBranchOptions() {
        try {
            return dao.findBranchOptions();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the tai danh sach chi nhanh: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BookingSportTypeOption> getSportTypeOptions(String branchId) {
        try {
            return dao.findSportTypeOptions(branchId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong thể tải danh sách loại thể thao: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BookingAreaOption> getAreaOptions(String branchId, String sportTypeIdOrNull) {
        try {
            return dao.findAreaOptions(branchId, sportTypeIdOrNull);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the tai danh sach khu vuc: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BookingCourtOption> getCourtsByArea(String areaId) {
        try {
            return dao.findCourtsByArea(areaId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the tai danh sach san: " + e.getMessage(), e);
        }
    }

    @Override
    public BookingOpenHours getOpenHours() {
        try {
            return dao.getOpenHours();
        } catch (SQLException e) {
            e.printStackTrace();
            return BookingOpenHours.defaultHours();
        }
    }

    @Override
    public List<BookingSlotDTO> getBookings(String branchId, String areaId, LocalDate date) {
        // Default to null for bookingStatus if not specified, allowing the DAO to handle it
        return getBookings(branchId, areaId, date, null);
    }

    @Override
    public List<BookingSlotDTO> getBookings(String branchId, String areaId, LocalDate date, String bookingStatus) {
        try {
            return dao.findBookings(branchId, areaId, date, bookingStatus);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the tai lich dat san: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SportTypeAreaOption> getSportTypeAreaOptionsByBranch(String branchId) {
        try {
            // Get all sport types for the branch
            List<BookingSportTypeOption> sportTypes = dao.findSportTypeOptions(branchId);
            // Get all areas for the branch
            List<BookingAreaOption> areas = dao.findAreaOptions(branchId, null); // Pass null to get all areas regardless of sport type

            // Group areas by sportTypeId
            Map<String, List<String>> areasBySportType = areas.stream()
                    .collect(Collectors.groupingBy(
                            BookingAreaOption::sportTypeId,
                            Collectors.mapping(BookingAreaOption::areaId, Collectors.toList())
                    ));

            // Combine sport types with their respective areas
            return sportTypes.stream()
                    .map(sportType -> new SportTypeAreaOption(
                            sportType.sportTypeId(),
                            sportType.sportTypeName(),
                            areasBySportType.getOrDefault(sportType.sportTypeId(), List.of())
                    ))
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong the tai danh sach loai the thao va khu vuc cho chi nhanh: " + e.getMessage(), e);
        }
    }
    @Override
    public BookingInvoiceDTO getInvoiceDetails(String invoiceId) {
        try {
            return dao.getInvoiceDetails(invoiceId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Không thể tải chi tiết hóa đơn: " + e.getMessage(), e);
        }
    }
    @Override
    public List<BookingSlotDTO> getBookingsByInvoiceId(String invoiceId) {
        try {
            return dao.findBookingsByInvoiceId(invoiceId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Không thể tải chi tiết hóa đơn: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PendingBookingRequestDTO> getPendingDepositRequests(String branchIdOrNull,
                                                                    LocalDate dateOrNull,
                                                                    String customerPhoneOrNull) {
        try {
            return dao.findPendingDepositRequests(branchIdOrNull, dateOrNull, customerPhoneOrNull);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Không thể tải yêu cầu đã cọc chờ xác nhận: " + e.getMessage(), e);
        }
    }

    @Override
    public int countPendingDepositRequests(String branchIdOrNull) {
        try {
            return dao.countPendingDepositRequests(branchIdOrNull);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean confirmPendingDepositBooking(String invoiceId) {
        try {
            return dao.confirmPendingDepositBooking(invoiceId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean cancelBooking(String invoiceId) {
        try {
            return dao.cancelBookingInvoice(invoiceId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
