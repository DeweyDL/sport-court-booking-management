package com.sportcourt.modules.customer_history.dto;

import java.math.BigDecimal;

public class PriceBoardOptionDTO {

    private String priceBoardId;
    private int startHour;
    private int endHour;
    private BigDecimal price;

    public PriceBoardOptionDTO() {
    }

    public String getPriceBoardId() {
        return priceBoardId;
    }

    public void setPriceBoardId(String priceBoardId) {
        this.priceBoardId = priceBoardId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%s (%02d:00 - %02d:00) - %,.0fđ", priceBoardId, startHour, endHour, price).replace(",", ".");
    }
}