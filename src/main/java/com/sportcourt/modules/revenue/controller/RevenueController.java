package com.sportcourt.modules.revenue.controller;

import com.sportcourt.modules.revenue.dto.BranchRevenueRow;
import com.sportcourt.modules.revenue.dto.CourtRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;
import com.sportcourt.modules.revenue.dto.ServiceRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueChartData;
import com.sportcourt.modules.revenue.dto.RevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueSearchCriteria;
import com.sportcourt.modules.revenue.dto.RevenueSummary;
import com.sportcourt.modules.revenue.service.RevenueService;
import com.sportcourt.modules.revenue.service.RevenueServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController() {
        this(new RevenueServiceImpl());
    }

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    public List<RevenueRow> search(RevenueSearchCriteria criteria) throws SQLException {
        return revenueService.search(criteria);
    }

    public RevenueSummary getSummary(RevenueSearchCriteria criteria) throws SQLException {
        return revenueService.getSummary(criteria);
    }

    public RevenueChartData getChartData(RevenueSearchCriteria criteria) throws SQLException {
        return revenueService.getChartData(criteria);
    }

    public List<BranchRevenueRow> getBranchRevenue(RevenueSearchCriteria criteria) throws SQLException {
        return revenueService.getBranchRevenue(criteria);
    }

    public List<CourtRevenueRow> getCourtRevenue(RevenueSearchCriteria criteria) throws SQLException {
        return revenueService.getCourtRevenue(criteria);
    }

    public List<ServiceRevenueRow> getServiceRevenue(RevenueSearchCriteria criteria) throws SQLException {
        return revenueService.getServiceRevenue(criteria);
    }

    public void create(RevenueCreateRequest request) throws SQLException {
        revenueService.create(request);
    }

    public String generateNextId() throws SQLException {
        return revenueService.generateNextId();
    }
}
