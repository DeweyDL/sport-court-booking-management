package com.sportcourt.modules.customer_history.controller;

import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.customer_history.service.BookingHistoryService;
import com.sportcourt.modules.customer_history.service.BookingHistoryServiceImpl;

import java.util.List;

/**
 * Controller cho module lịch sử đặt sân.
 * Nhận lệnh từ View, gọi Service, trả kết quả về View.
 * Không chứa logic nghiệp vụ.
 */
public class BookingHistoryController {

    private final BookingHistoryService service;

    public BookingHistoryController() {
        this.service = new BookingHistoryServiceImpl();
    }

    /** Constructor injection cho test / DI */
    public BookingHistoryController(BookingHistoryService service) {
        this.service = service;
    }

    // ====================================================================

    /**
     * Tải danh sách lịch sử đặt sân của khách hàng.
     *
     * @param customerId mã khách hàng (MAKH), lấy từ session
     * @param keyword    từ khóa tìm kiếm (có thể null/rỗng)
     * @return danh sách BookingHistoryItemDTO
     */
    public List<BookingHistoryItemDTO> loadHistory(String customerId, String keyword) {
        return service.getBookingHistory(customerId, keyword);
    }

    /**
     * Tải chi tiết một hóa đơn đặt sân.
     *
     * @param invoiceId mã hóa đơn (MAHD)
     * @return BookingDetailDTO đầy đủ
     */
    public BookingDetailDTO loadDetail(String invoiceId) {
        return service.getBookingDetail(invoiceId);
    }
}