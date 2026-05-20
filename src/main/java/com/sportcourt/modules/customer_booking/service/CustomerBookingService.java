package com.sportcourt.modules.customer_booking.service;

import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchCriteria;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.CreateBookingRequest;
import com.sportcourt.modules.customer_booking.dto.PriceSlot;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;
import com.sportcourt.modules.customer_booking.dto.SportTypeOption;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerBookingService {
    List<BranchOption> findAvailableBranches();

    List<SportTypeOption> findAvailableSportTypes(String branchId);

    List<CourtSearchResult> searchCourts(CourtSearchCriteria criteria);

    Optional<CourtSearchResult> findCourtDetail(String courtId);

    List<PriceSlot> findPriceBoardsByArea(String areaId);

    List<SlotStatus> findAvailableSlots(String courtId, LocalDate bookingDate);

    List<SlotStatus> findBookedSlots(String areaId, LocalDate bookingDate);

    List<String> findCourtsByArea(String areaId);

    boolean isSlotAvailable(String courtId, String priceBoardId, LocalDate bookingDate);

    String findCustomerByUserId(String userId);

    Optional<String> findBookingEmployeeByBranch(String branchId);

    BookingPreview createPendingInvoice(CreateBookingRequest request);

    void insertCourtBookingDetails(String invoiceId, List<SelectedBookingSlot> selectedBookingSlots);

    void cancelPendingBooking(String invoiceId);

    void markPendingInvoiceAsDeposited(String invoiceId);

    int expireStalePendingBookings();
}
