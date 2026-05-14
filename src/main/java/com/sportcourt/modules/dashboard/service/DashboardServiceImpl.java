package com.sportcourt.modules.dashboard.service;

import com.sportcourt.modules.dashboard.dao.DashboardDAO;
import com.sportcourt.modules.dashboard.dao.DashboardDAOImpl;
import com.sportcourt.modules.dashboard.dto.DashboardCourtCard;
import com.sportcourt.modules.dashboard.dto.DashboardSportTypeOption;

import java.sql.SQLException;
import java.util.List;

public class DashboardServiceImpl implements DashboardService {
    private final DashboardDAO dao;

    public DashboardServiceImpl() {
        this(new DashboardDAOImpl());
    }

    public DashboardServiceImpl(DashboardDAO dao) {
        this.dao = dao;
    }

    @Override
    public List<DashboardCourtCard> searchCourts(String keyword) throws SQLException {
        return dao.searchCourts(keyword);
    }

    @Override
    public List<DashboardCourtCard> getCourtsBySportType(String sportTypeId) throws SQLException {
        if (sportTypeId == null || sportTypeId.isBlank()) {
            throw new SQLException("Sport type id is required.");
        }
        return dao.findCourtsBySportType(sportTypeId.trim());
    }

    @Override
    public List<DashboardCourtCard> getCourtsBySportTypeName(String sportTypeName) throws SQLException {
        if (sportTypeName == null || sportTypeName.isBlank()) {
            throw new SQLException("Sport type name is required.");
        }
        return dao.findCourtsBySportTypeName(sportTypeName.trim());
    }

    @Override
    public List<DashboardSportTypeOption> getAvailableSportTypes() throws SQLException {
        return dao.findAvailableSportTypes();
    }
}
