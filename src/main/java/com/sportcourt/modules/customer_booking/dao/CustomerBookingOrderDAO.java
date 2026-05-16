package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.CreateBookingRequest;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerBookingOrderDAO {
    public String findCustomerByUserId(String userId) throws SQLException;
    public Optional<String> findBookingEmployeeByBranch(String branchId) throws SQLException;
    public BookingPreview createPendingInvoice(CreateBookingRequest request) throws SQLException;
    public void insertCourtBookingDetails(String invoiceId, List<SelectedBookingSlot> selectedBookingSlot) throws SQLException;
    public void cancelPendingBooking(String invoiceId) throws SQLException;
}
