package com.sportcourt.modules.customer_history.dao;

import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import java.util.List;

/**
 * DAO interface cho module lịch sử đặt sân của khách hàng.
 */
public interface BookingHistoryDAO {

    /**
     * Lấy danh sách hóa đơn đặt sân của một khách hàng.
     * Hỗ trợ lọc theo từ khóa (tên chi nhánh, loại thể thao, mã hóa đơn).
     *
     * @param customerId mã khách hàng (MAKH)
     * @param keyword    từ khóa tìm kiếm, null hoặc rỗng để lấy tất cả
     * @return danh sách BookingHistoryItemDTO, sắp xếp theo ngày tạo mới nhất
     */
    List<BookingHistoryItemDTO> findByCustomerId(String customerId, String keyword);

    /**
     * Lấy chi tiết một hóa đơn đặt sân theo mã hóa đơn.
     *
     * @param invoiceId mã hóa đơn (MAHD)
     * @return BookingDetailDTO hoặc null nếu không tìm thấy
     */
    BookingDetailDTO findDetailByInvoiceId(String invoiceId);
}