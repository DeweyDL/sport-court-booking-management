package com.sportcourt.modules.customer_history.service;

import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.util.List;

public interface BookingHistoryService {

    void cancelCourtBooking(String detailId);

    List<BookingHistoryItemDTO> getBookingHistory(String customerId, String keyword);

    BookingDetailDTO getBookingDetail(String invoiceId);

}