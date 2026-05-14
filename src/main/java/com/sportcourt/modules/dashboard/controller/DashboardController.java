package com.sportcourt.modules.dashboard.controller;

import com.sportcourt.modules.dashboard.dto.DashboardCourtCard;
import com.sportcourt.modules.dashboard.dto.DashboardSportTypeOption;
import com.sportcourt.modules.dashboard.service.DashboardService;
import com.sportcourt.modules.dashboard.service.DashboardServiceImpl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DashboardController {
    private final DashboardService service;

    public DashboardController() {
        this(new DashboardServiceImpl());
    }

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    public List<DashboardCourtCard> search(String keyword) {
        try {
            return service.searchCourts(keyword);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<DashboardCourtCard> filterBySportType(String sportTypeId) {
        try {
            return service.getCourtsBySportType(sportTypeId);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<DashboardCourtCard> filterBySportTypeName(String sportTypeName) {
        try {
            return service.getCourtsBySportTypeName(sportTypeName);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<DashboardSportTypeOption> loadAvailableSportTypes() {
        try {
            return service.getAvailableSportTypes();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
