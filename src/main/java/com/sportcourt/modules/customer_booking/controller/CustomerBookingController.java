package com.sportcourt.modules.customer_booking.controller;

import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.customer_booking.dto.*;
import com.sportcourt.modules.customer_booking.service.CustomerBookingService;
import com.sportcourt.modules.customer_booking.service.CustomerBookingServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerBookingController {
    private final CustomerBookingService customerBookingService;

    public CustomerBookingController() {
        this(new CustomerBookingServiceImpl());
    }

    public CustomerBookingController(CustomerBookingService customerBookingService) {
        this.customerBookingService = customerBookingService;
    }

    public List<BranchOption> loadBranches() {
        return customerBookingService.findAvailableBranches();
    }

    public List<SportTypeOption> loadSportTypes(String branchId) {
        return customerBookingService.findAvailableSportTypes(branchId);
    }

    public List<CourtSearchResult> searchCourts(String keyword,
                                                String branchId,
                                                String sportTypeId,
                                                CourtSortBy sortBy,
                                                boolean ascending) {
        return customerBookingService.searchCourts(new CourtSearchCriteria(
                keyword,
                branchId,
                sportTypeId,
                sortBy,
                ascending ? "ASC" : "DESC"
        ));
    }

    public List<SlotStatus> loadSlots(String courtId, LocalDate bookingDate) {
        return customerBookingService.findAvailableSlots(courtId, bookingDate);
    }

    public List<CourtSchedule> loadAreaSchedule(String areaId, LocalDate date) {
        List<String> courtIds = customerBookingService.findCourtsByArea(areaId);
        return courtIds.stream()
                .map(id -> new CourtSchedule(id, id,
                        customerBookingService.findAvailableSlots(id, date)))
                .collect(Collectors.toList());
    }

    public List<Integer> loadPriceHours(String areaId) {
        return customerBookingService.findPriceBoardsByArea(areaId).stream()
                .map(PriceSlot::startHour)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Optional<CourtSearchResult> findCourt(String courtId) {
        return customerBookingService.findCourtDetail(courtId);
    }

    public BookingPreview createBooking(String branchId, List<SelectedBookingSlot> selectedSlots) {
        UserSession session = SessionManager.requireSession();
        String customerId = resolveCustomerId(session);
        String employeeId = resolveEmployeeId(session, branchId);

        return customerBookingService.createPendingInvoice(new CreateBookingRequest(
                customerId,
                employeeId,
                selectedSlots,
                computeDeposit(selectedSlots),
                BigDecimal.ZERO
        ));
    }

    public void cancelPendingBooking(String invoiceId) {
        customerBookingService.cancelPendingBooking(invoiceId);
    }

    public void markBookingAsDeposited(String invoiceId) {
        customerBookingService.markPendingInvoiceAsDeposited(invoiceId);
    }

    private String resolveCustomerId(UserSession session) {
        String customerId = session.getCustomerId();
        if (customerId != null && !customerId.isBlank()) {
            return customerId;
        }

        customerId = customerBookingService.findCustomerByUserId(session.getUserId());
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalStateException("Tài khoản hiện tại không phải khách hàng hợp lệ.");
        }

        return customerId;
    }

    private String resolveEmployeeId(UserSession session, String branchId) {
        String employeeId = session.getEmployeeId();
        if (employeeId != null && !employeeId.isBlank()) {
            return employeeId;
        }

        return customerBookingService.findBookingEmployeeByBranch(branchId)
                .orElseThrow(() -> new IllegalStateException(
                        "Chi nhánh này chưa có nhân viên đang hoạt động để xử lý đặt sân."
                ));
    }

    private BigDecimal computeDeposit(List<SelectedBookingSlot> selectedSlots) {
        BigDecimal total = selectedSlots == null ? BigDecimal.ZERO : selectedSlots.stream()
                .map(SelectedBookingSlot::unitPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
    }
}
