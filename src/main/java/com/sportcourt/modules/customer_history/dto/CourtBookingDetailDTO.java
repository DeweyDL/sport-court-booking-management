package com.sportcourt.modules.customer_history.dto;

import java.time.LocalDateTime;

public class CourtBookingDetailDTO {

    private String invoiceId;
    private String courtId;
    private int startHour;
    private int endHour;
    private LocalDateTime playDate;
    private double price;

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

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public LocalDateTime getPlayDate() {
        return playDate;
    }

    public void setPlayDate(LocalDateTime playDate) {
        this.playDate = playDate;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}