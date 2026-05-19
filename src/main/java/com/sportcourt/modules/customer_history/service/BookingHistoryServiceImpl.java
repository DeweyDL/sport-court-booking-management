package com.sportcourt.modules.customer_history.service;

import com.sportcourt.modules.customer_history.dao.BookingHistoryDAO;
import com.sportcourt.modules.customer_history.dao.JdbcBookingHistoryDAO;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.util.List;

public class BookingHistoryServiceImpl implements BookingHistoryService {

    private final BookingHistoryDAO dao;

    public BookingHistoryServiceImpl() {
        this.dao = new JdbcBookingHistoryDAO();
    }

    public BookingHistoryServiceImpl(BookingHistoryDAO dao) {
        this.dao = dao;
    }

    @Override
    public void confirmCourtBooking(String detailId) {
        if (detailId == null || detailId.isBlank()) {
            throw new IllegalArgumentException("Mã chi tiết thuê sân không hợp lệ.");
        }
        dao.confirmCourtBooking(detailId);
    }

    @Override
    public void markDeposited(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        dao.markDeposited(invoiceId);
    }

    @Override
    public List<BookingHistoryItemDTO> getBookingHistory(String customerId, String keyword) {
        return dao.findByCustomerId(customerId, keyword);
    }

    @Override
    public BookingDetailDTO getBookingDetail(String invoiceId) {
        return dao.findDetailByInvoiceId(invoiceId);
    }

    @Override
    public void cancelCourtBooking(String detailId) {
        if (detailId == null || detailId.isBlank()) {
            throw new IllegalArgumentException("Mã chi tiết thuê sân không hợp lệ.");
        }
        dao.cancelCourtBooking(detailId);
    }
}