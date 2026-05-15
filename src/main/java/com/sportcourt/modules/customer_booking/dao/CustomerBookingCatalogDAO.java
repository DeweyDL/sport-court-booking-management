package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.modules.court.dto.CourtSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface CustomerBookingCatalogDAO {
    public List<Optional> findAvailableBranches();
    public List<Optional> findAvailableSportTypes(String branchId);
    public List<Optional> searchCourts(CourtSearchCriteria criteria);
    public Optional findCourtDetail(String courtId);
}
