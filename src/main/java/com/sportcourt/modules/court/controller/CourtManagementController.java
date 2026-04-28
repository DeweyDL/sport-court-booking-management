package com.sportcourt.modules.court.controller;

import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;
import com.sportcourt.modules.court.service.CourtService;
import com.sportcourt.modules.court.service.CourtServiceImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CourtManagementController {
    private final CourtService courtService;

    public CourtManagementController() {
        this(new CourtServiceImpl());
    }

    public CourtManagementController(CourtService courtService) {
        this.courtService = courtService;
    }

    public List<CourtTableRow> search(CourtSearchCriteria criteria) throws SQLException {
        return courtService.search(criteria);
    }

    public Optional<CourtTableRow> findDetail(String courtId, String branchId) throws SQLException {
        return courtService.findDetail(courtId, branchId);
    }

    public List<String> getAreaIdsByBranch(String branchId) throws SQLException {
        return courtService.getAreaIdsByBranch(branchId);
    }

    public void create(Court court, String branchId) throws SQLException {
        courtService.create(court, branchId);
    }

    public void update(Court court, String branchId) throws SQLException {
        courtService.update(court, branchId);
    }

    public void delete(String courtId, String branchId) throws SQLException {
        courtService.delete(courtId, branchId);
    }
}
