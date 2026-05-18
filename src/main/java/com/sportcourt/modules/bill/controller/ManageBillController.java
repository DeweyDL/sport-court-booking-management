package com.sportcourt.modules.bill.controller;

import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.ServiceItem;
import com.sportcourt.modules.bill.service.ManageBillService;
import com.sportcourt.modules.bill.service.ManageBillServiceImpl;
import com.sportcourt.modules.customer_booking.dto.SelectedBookingSlot;

import java.util.List;

public class ManageBillController {
    private final ManageBillService service;

    public ManageBillController() {
        this(new ManageBillServiceImpl());
    }

    public ManageBillController(ManageBillService service) {
        this.service = service;
    }

    public BillResult<List<BillSummary>> searchBills(String keyword) {
        return service.searchBills(keyword);
    }

    public BillResult<BillDetail> getDetail(String maHD) {
        return service.getDetail(maHD);
    }

    public BillResult<Void> markAsPaid(String maHD) {
        return service.markAsPaid(maHD);
    }

    public BillResult<Void> cancelBill(String maHD) {
        return service.cancelBill(maHD);
    }

    public BillResult<Void> softDelete(String maHD) {
        return service.softDelete(maHD);
    }

    public BillResult<String> createEmptyBill(String maKH, String maNV) {
        return service.createEmptyBill(maKH, maNV);
    }

    public BillResult<Void> addCourtBookingDetails(String maHD, List<SelectedBookingSlot> slots, boolean advanceBooking) {
        return service.addCourtBookingDetails(maHD, slots, advanceBooking);
    }

    public BillResult<Void> addServiceItems(String maHD, List<ServiceItem> items) {
        return service.addServiceItems(maHD, items);
    }

    public BillResult<Void> updateServiceItemQty(String maCTHDDV, int newQty) {
        return service.updateServiceItemQty(maCTHDDV, newQty);
    }

    public BillResult<Void> updateDiscount(String maHD, int discountPercent) {
        return service.updateDiscount(maHD, discountPercent);
    }

    public BillResult<Void> deleteCourtRental(String maCTHDTS) {
        return service.deleteCourtRental(maCTHDTS);
    }
}
