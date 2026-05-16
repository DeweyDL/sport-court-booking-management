package com.sportcourt.modules.customer_booking.dao;

import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchCriteria;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SportTypeOption;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public interface CustomerBookingCatalogDAO {
    public List<BranchOption> findAvailableBranches() throws SQLException;
    public List<SportTypeOption> findAvailableSportTypes(String branchId) throws SQLException;
    public List<CourtSearchResult> searchCourts(CourtSearchCriteria criteria) throws SQLException;
    public Optional<CourtSearchResult> findCourtDetail(String courtId) throws SQLException;
}
