package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.modules.customer_booking.dto.PriceSlot;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface CustomerBookingScheduleDAO {
    public List<PriceSlot> findPriceBoardsByArea(String areaId) throws SQLException;
    public List<SlotStatus> findAvailableSlots(String courtId, LocalDate bookingDate) throws SQLException;
    public List<SlotStatus> findBookedSlots(String areaId, LocalDate bookingDate) throws SQLException;
    public List<String> findCourtsByArea(String areaId) throws SQLException;
    public boolean isSlotAvailable(String courtId, String priceBoardId, LocalDate bookingDate) throws SQLException;
}
