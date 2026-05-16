package com.sportcourt.modules.booking_management.controller;

import com.sportcourt.modules.booking_management.dto.*;
import com.sportcourt.modules.booking_management.service.BookingRequestService;
import com.sportcourt.modules.booking_management.service.BookingRequestServiceImpl;

import java.time.LocalDate;
import java.util.List;

public class BookingRequestController {
    private final BookingRequestService service;

    public BookingRequestController() {
        this(new BookingRequestServiceImpl());
    }

    public BookingRequestController(BookingRequestService service) {
        this.service = service;
    }

    public List<BookingBranchOption> getBranchOptions() {
        return service.getBranchOptions();
    }

    public List<BookingSportTypeOption> getSportTypeOptions(String branchId) {
        return service.getSportTypeOptions(branchId);
    }

    public List<BookingAreaOption> getAreaOptions(String branchId, String sportTypeIdOrNull) {
        return service.getAreaOptions(branchId, sportTypeIdOrNull);
    }

    public List<BookingCourtOption> getCourtsByArea(String areaId) {
        return service.getCourtsByArea(areaId);
    }

    public BookingOpenHours getOpenHours() {
        return service.getOpenHours();
    }

    public List<BookingSlotDTO> getBookings(String branchId, String areaId, LocalDate date) {
        return service.getBookings(branchId, areaId, date);
    }

    public List<BookingSlotDTO> getBookings(String branchId, String areaId, LocalDate date, String bookingStatus) {
        return service.getBookings(branchId, areaId, date, bookingStatus);
    }

    public List<SportTypeAreaOption> getSportTypeAreaOptionsByBranch(String branchId) {
        return service.getSportTypeAreaOptionsByBranch(branchId);
    }

    public BookingInvoiceDTO getInvoiceDetails(String invoiceId) {
        return service.getInvoiceDetails(invoiceId);
    }

    public List<BookingSlotDTO> getBookingsByInvoiceId(String invoiceId) {
        return service.getBookingsByInvoiceId(invoiceId);
    }

    public boolean cancelBooking(String id) {
        return service.cancelBooking(id); // ID này tự động chuyển tiếp xuống DAO xử lý MACT_THUE_SAN
    }


}