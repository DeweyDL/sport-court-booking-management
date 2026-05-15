package com.sportcourt.modules.customer_booking.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerBookingScheduleDAO {
    public List<Optional> findPriceBoardsByArea(String areaId);
    public List<Optional> findAvailableSlots(String courtId, LocalDate bookingDate);
    public List<Optional> findBookedSlots(String areaId, LocalDate bookingDate);
    public String findCourtsByArea(String areaId);
    public boolean isSlotAvailable(String courtId, String priceBoardId, LocalDate bookingDate);
}
