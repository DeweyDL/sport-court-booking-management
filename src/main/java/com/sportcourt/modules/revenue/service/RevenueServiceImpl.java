package com.sportcourt.modules.revenue.service;

import com.sportcourt.modules.auth.dto.FunctionId;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.service.PermissionService;
import com.sportcourt.modules.auth.service.PermissionServiceImpl;
import com.sportcourt.modules.revenue.dao.JdbcRevenueDAO;
import com.sportcourt.modules.revenue.dao.RevenueDAO;
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

public class RevenueServiceImpl implements RevenueService {

    private final RevenueDAO revenueDAO;
    private final PermissionService permissionService;

    public RevenueServiceImpl() {
        this(new JdbcRevenueDAO(), new PermissionServiceImpl());
    }

    public RevenueServiceImpl(RevenueDAO revenueDAO, PermissionService permissionService) {
        this.revenueDAO = revenueDAO;
        this.permissionService = permissionService;
    }

    @Override
    public List<RevenueRow> search(RevenueSearchCriteria criteria) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.findRows(criteria);
    }

    @Override
    public RevenueSummary getSummary(RevenueSearchCriteria criteria) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.getSummary(criteria);
    }

    @Override
    public RevenueChartData getChartData(RevenueSearchCriteria criteria) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.getChartData(criteria);
    }

    @Override
    public List<BranchRevenueRow> getBranchRevenue(RevenueSearchCriteria criteria) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.getBranchRevenue(criteria);
    }

    @Override
    public List<CourtRevenueRow> getCourtRevenue(RevenueSearchCriteria criteria) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.getCourtRevenue(criteria);
    }

    @Override
    public List<ServiceRevenueRow> getServiceRevenue(RevenueSearchCriteria criteria) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.getServiceRevenue(criteria);
    }

    @Override
    public void create(RevenueCreateRequest request) throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.ADD);
        revenueDAO.create(request);
    }

    @Override
    public String generateNextId() throws SQLException {
        permissionService.requirePermission(FunctionId.REVENUE_MANAGEMENT, PermissionAction.VIEW);
        return revenueDAO.generateNextId();
    }
}
