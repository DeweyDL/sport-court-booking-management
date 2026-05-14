package com.sportcourt.modules.customer_history.service;

import com.sportcourt.modules.customer_history.dao.BookingHistoryDAO;
import com.sportcourt.modules.customer_history.dao.JdbcBookingHistoryDAO;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.util.List;

/**
 * Triển khai BookingHistoryService.
 * Lớp này đóng vai trò trung gian giữa Controller và DAO,
 * có thể thêm validation / business logic ở đây.
 */
public class BookingHistoryServiceImpl implements BookingHistoryService {

    private final BookingHistoryDAO dao;

    /** Constructor mặc định — dùng JDBC DAO thật. */
    public BookingHistoryServiceImpl() {
        this.dao = new JdbcBookingHistoryDAO();
    }

    /** Constructor injection — cho phép mock trong test. */
    public BookingHistoryServiceImpl(BookingHistoryDAO dao) {
        this.dao = dao;
    }

    // ====================================================================
    @Override
    public List<BookingHistoryItemDTO> getBookingHistory(String customerId, String keyword) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId không được rỗng.");
        }
        return dao.findByCustomerId(customerId, keyword);
    }

    // ====================================================================
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
}