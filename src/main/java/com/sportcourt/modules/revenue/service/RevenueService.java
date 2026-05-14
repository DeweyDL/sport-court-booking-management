package com.sportcourt.modules.revenue.service;

import com.sportcourt.modules.revenue.dto.BranchRevenueRow;
import com.sportcourt.modules.revenue.dto.CourtRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;
import com.sportcourt.modules.revenue.dto.ServiceRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueChartData;
import com.sportcourt.modules.revenue.dto.RevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueSearchCriteria;
import com.sportcourt.modules.revenue.dto.RevenueSummary;

import java.sql.SQLException;
import java.util.List;

public interface RevenueService {
    List<RevenueRow> search(RevenueSearchCriteria criteria) throws SQLException;
    RevenueSummary getSummary(RevenueSearchCriteria criteria) throws SQLException;
    RevenueChartData getChartData(RevenueSearchCriteria criteria) throws SQLException;
    List<BranchRevenueRow> getBranchRevenue(RevenueSearchCriteria criteria) throws SQLException;
    List<CourtRevenueRow> getCourtRevenue(RevenueSearchCriteria criteria) throws SQLException;
    List<ServiceRevenueRow> getServiceRevenue(RevenueSearchCriteria criteria) throws SQLException;
    void create(RevenueCreateRequest request) throws SQLException;
    String generateNextId() throws SQLException;
}
