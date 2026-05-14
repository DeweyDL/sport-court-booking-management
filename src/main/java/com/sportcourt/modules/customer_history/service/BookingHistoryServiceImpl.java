package com.sportcourt.modules.customer_history.service;

import com.sportcourt.modules.customer_history.dao.BookingHistoryDAO;
import com.sportcourt.modules.customer_history.dao.JdbcBookingHistoryDAO;
import com.sportcourt.modules.customer_history.dto.BookingAddCourtRequest;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.customer_history.dto.PriceBoardOptionDTO;

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
    public List<BookingHistoryItemDTO> getBookingHistory(String customerId, String keyword) {
        return dao.findByCustomerId(customerId, keyword);
    }

    @Override
    public BookingDetailDTO getBookingDetail(String invoiceId) {
        if (invoiceId == null || invoiceId.isBlank()) {
            throw new IllegalArgumentException("invoiceId không được rỗng.");
        }
        BookingDetailDTO detail = dao.findDetailByInvoiceId(invoiceId);
        if (detail == null) {
            throw new RuntimeException("Không tìm thấy hóa đơn: " + invoiceId);
        }
        return detail;
    }

    @Override
    public List<String> getAvailableCourtIds() {
        return dao.getAvailableCourtIds();
    }

    @Override
    public List<PriceBoardOptionDTO> getAvailablePriceBoards() {
        return dao.getAvailablePriceBoards();
    }

    @Override
    public void addCourtBooking(BookingAddCourtRequest request) {
        if (request == null || request.getInvoiceId() == null || request.getCourtId() == null || request.getPriceBoardId() == null || request.getBookingDateStr() == null) {
            throw new IllegalArgumentException("Dữ liệu đầu vào không hợp lệ.");
        }
        dao.addCourtBooking(request);
    }
}