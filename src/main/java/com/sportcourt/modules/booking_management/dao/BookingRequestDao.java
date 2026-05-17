package com.sportcourt.modules.booking_management.dao;

import com.sportcourt.modules.booking_management.dto.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface BookingRequestDao {

    List<BookingBranchOption> findBranchOptions() throws SQLException;

    List<BookingSportTypeOption> findSportTypeOptions(String branchId) throws SQLException;

    List<BookingAreaOption> findAreaOptions(String branchId, String sportTypeIdOrNull) throws SQLException;

    List<BookingCourtOption> findCourtsByArea(String areaId) throws SQLException;

    BookingOpenHours getOpenHours() throws SQLException;

    List<BookingSlotDTO> findBookings(String branchId, String areaId, LocalDate date, String bookingStatus) throws SQLException;

    List<BookingSlotDTO> findBookingsByInvoiceId(String invoiceId) throws SQLException;

    List<PendingBookingRequestDTO> findPendingDepositRequests(
            String branchIdOrNull,
            LocalDate dateOrNull,
            String customerPhoneOrNull
    ) throws SQLException;

    int countPendingDepositRequests(String branchIdOrNull) throws SQLException;

    BookingInvoiceDTO getInvoiceDetails(String invoiceId) throws SQLException;

    boolean confirmPendingDepositBooking(String invoiceId) throws SQLException;

    boolean cancelBookingInvoice(String bookingDetailId) throws SQLException;
}
