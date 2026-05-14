package com.sportcourt.modules.customer_history.service;

import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.util.List;

/**
 * Service interface cho module lịch sử đặt sân.
 */
public interface BookingHistoryService {

    /**
     * Truy xuất danh sách hóa đơn đặt sân của khách hàng.
     * Có thể lọc theo từ khóa (mã hóa đơn, tên chi nhánh, loại thể thao, trạng thái).
     *
     * @param customerId mã khách hàng (MAKH)
     * @param keyword    từ khóa tìm kiếm, null hoặc rỗng để lấy tất cả
     * @return danh sách BookingHistoryItemDTO sắp xếp mới nhất trước
     * @throws RuntimeException nếu lỗi truy vấn DB
     */
    List<BookingHistoryItemDTO> getBookingHistory(String customerId, String keyword);

    /**
     * Truy xuất chi tiết một hóa đơn đặt sân.
     *
     * @param invoiceId mã hóa đơn (MAHD)
     * @return BookingDetailDTO đầy đủ thông tin
     * @throws RuntimeException nếu không tìm thấy hoặc lỗi DB
     */
    BookingDetailDTO getBookingDetail(String invoiceId);
}