package com.sportcourt.modules.booking_management.service;

import com.sportcourt.modules.booking_management.dto.*;
import java.time.LocalDate;
import java.util.List;

public interface BookingRequestService {
    List<BookingBranchOption> getBranchOptions();
    List<BookingSportTypeOption> getSportTypeOptions(String branchId);
    List<BookingAreaOption> getAreaOptions(String branchId, String sportTypeIdOrNull);
    List<BookingCourtOption> getCourtsByArea(String areaId);
    BookingOpenHours getOpenHours();
    List<BookingSlotDTO> getBookings(String branchId, String areaId, LocalDate date);
    List<BookingSlotDTO> getBookings(String branchId, String areaId, LocalDate date, String bookingStatus);
    List<SportTypeAreaOption> getSportTypeAreaOptionsByBranch(String branchId);
    BookingInvoiceDTO getInvoiceDetails(String invoiceId);
    List<BookingSlotDTO> getBookingsByInvoiceId(String invoiceId);
    List<PendingBookingRequestDTO> getPendingDepositRequests(String branchIdOrNull, LocalDate dateOrNull, String customerPhoneOrNull);
    int countPendingDepositRequests(String branchIdOrNull);
    boolean confirmPendingDepositBooking(String invoiceId);

    boolean cancelBooking(String invoiceId);

    boolean checkInBooking(String invoiceId);
}
