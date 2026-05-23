package com.sportcourt.modules.bill.dao;

import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.ServiceItem;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ManageBillDao {
    List<BillSummary> findAll(String keyword, String branchId) throws SQLException;
    Optional<BillDetail> findDetailById(String maHD) throws SQLException;
    boolean updateStatus(String maHD, String newStatus, String requiredCurrentStatus) throws SQLException;
    boolean markDepositPaid(String maHD) throws SQLException;
    boolean softDelete(String maHD) throws SQLException;
    String createEmptyBill(String maKH, String maNV) throws SQLException;
    void addCourtBookingDetails(String maHD, List<SelectedBookingSlot> slots, boolean advanceBooking) throws SQLException;
    void addServiceItems(String maHD, List<ServiceItem> items) throws SQLException;
    void updateServiceItemQty(String maCTHDDV, int newQty) throws SQLException;
    void deleteCourtRental(String maCTHDTS) throws SQLException;
    void updateDiscount(String maHD, int discountPercent) throws SQLException;
}
