package com.sportcourt.modules.customer_history.dao;

import com.sportcourt.modules.customer_history.dto.BookingAddCourtRequest;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.customer_history.dto.PriceBoardOptionDTO;

import java.util.List;

public interface BookingHistoryDAO {

    List<BookingHistoryItemDTO> findByCustomerId(String customerId, String keyword);

    BookingDetailDTO findDetailByInvoiceId(String invoiceId);

    List<String> getAvailableCourtIds();

    List<PriceBoardOptionDTO> getAvailablePriceBoards();

    void addCourtBooking(BookingAddCourtRequest request);
}