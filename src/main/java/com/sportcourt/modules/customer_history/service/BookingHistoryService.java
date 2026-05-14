package com.sportcourt.modules.customer_history.service;

import com.sportcourt.modules.customer_history.dto.BookingAddCourtRequest;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.customer_history.dto.PriceBoardOptionDTO;

import java.util.List;

public interface BookingHistoryService {

    List<BookingHistoryItemDTO> getBookingHistory(String customerId, String keyword);

    BookingDetailDTO getBookingDetail(String invoiceId);

    List<String> getAvailableCourtIds();

    List<PriceBoardOptionDTO> getAvailablePriceBoards();

    void addCourtBooking(BookingAddCourtRequest request);
}