package com.sportcourt.modules.customer_history.controller;

import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.customer_history.service.BookingHistoryService;
import com.sportcourt.modules.customer_history.service.BookingHistoryServiceImpl;

import java.util.List;

public class BookingHistoryController {

    private final BookingHistoryService service;

    public BookingHistoryController() {
        this.service = new BookingHistoryServiceImpl();
    }

    public BookingHistoryController(BookingHistoryService service) {
        this.service = service;
    }

    public List<BookingHistoryItemDTO> loadHistory(String customerId, String keyword) {
        return service.getBookingHistory(customerId, keyword);
    }

    public BookingDetailDTO loadDetail(String invoiceId) {
        return service.getBookingDetail(invoiceId);
    }

    public void cancelCourtBooking(String detailId) {
        service.cancelCourtBooking(detailId);
    }

    public void confirmCourtBooking(String detailId) {
        service.confirmCourtBooking(detailId);
    }
}