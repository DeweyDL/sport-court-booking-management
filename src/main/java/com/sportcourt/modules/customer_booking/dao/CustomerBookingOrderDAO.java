package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.modules.customer_booking.dto.BookingPreview;
import com.sportcourt.modules.customer_booking.dto.CreateBookingRequest;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;

import java.util.List;
import java.util.Optional;

public interface CustomerBookingOrderDAO {
    public String findCustomerByUserId(String userId);
    public BookingPreview createPendingInvoice(CreateBookingRequest request);
    public void insertCourtBookingDetails(String invoiceId, List<SelectedBookingSlot> selectedBookingSlot);
    public void cancelPendingBooking(String invoiceId);
}
