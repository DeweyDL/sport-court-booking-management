package com.sportcourt.modules.customer_history.dto;

public class BookingAddCourtRequest {

    private String invoiceId;
    private String courtId;
    private String priceBoardId;
    private String bookingDateStr;

    public BookingAddCourtRequest() {
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public String getPriceBoardId() {
        return priceBoardId;
    }

    public void setPriceBoardId(String priceBoardId) {
        this.priceBoardId = priceBoardId;
    }

    public String getBookingDateStr() {
        return bookingDateStr;
    }

    public void setBookingDateStr(String bookingDateStr) {
        this.bookingDateStr = bookingDateStr;
    }
}