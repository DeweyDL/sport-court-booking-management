package com.sportcourt.modules.customer_history.dao;

import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.util.List;

public interface BookingHistoryDAO {

    void cancelCourtBooking(String detailId);

    List<BookingHistoryItemDTO> findByCustomerId(String customerId, String keyword);

    BookingDetailDTO findDetailByInvoiceId(String invoiceId);

}