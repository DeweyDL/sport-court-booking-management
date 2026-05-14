package com.sportcourt.modules.dashboard.service;

import com.sportcourt.modules.dashboard.dto.DashboardCourtCard;
import com.sportcourt.modules.dashboard.dto.DashboardSportTypeOption;

import java.sql.SQLException;
import java.util.List;

public interface DashboardService {
    List<DashboardCourtCard> searchCourts(String keyword) throws SQLException;

    List<DashboardCourtCard> getCourtsBySportType(String sportTypeId) throws SQLException;

    List<DashboardCourtCard> getCourtsBySportTypeName(String sportTypeName) throws SQLException;

    List<DashboardSportTypeOption> getAvailableSportTypes() throws SQLException;
}
