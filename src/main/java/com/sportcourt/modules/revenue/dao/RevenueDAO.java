package com.sportcourt.modules.revenue.dao;

import com.sportcourt.modules.revenue.dto.BranchRevenueRow;
import com.sportcourt.modules.revenue.dto.CourtRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueChartData;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;
import com.sportcourt.modules.revenue.dto.ServiceRevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueRow;
import com.sportcourt.modules.revenue.dto.RevenueSearchCriteria;
import com.sportcourt.modules.revenue.dto.RevenueSummary;

import java.sql.SQLException;
import java.util.List;

public interface RevenueDAO {

    /** Danh sách doanh thu có filter */
    List<RevenueRow> findRows(RevenueSearchCriteria criteria) throws SQLException;

    /** Tổng doanh thu, số báo cáo, lợi nhuận ước tính */
    RevenueSummary getSummary(RevenueSearchCriteria criteria) throws SQLException;

    /** Doanh thu theo ngày trong tuần (kỳ này vs kỳ trước) để vẽ biểu đồ */
    RevenueChartData getChartData(RevenueSearchCriteria criteria) throws SQLException;

    /** Doanh thu tổng hợp theo chi nhánh (để hiển thị hiệu suất) */
    List<BranchRevenueRow> getBranchRevenue(RevenueSearchCriteria criteria) throws SQLException;

    /** Doanh thu thuê sân theo từng sân con (SC-1, SC-2...) */
    List<CourtRevenueRow> getCourtRevenue(RevenueSearchCriteria criteria) throws SQLException;

    /** Doanh thu dịch vụ theo từng sản phẩm / dụng cụ */
    List<ServiceRevenueRow> getServiceRevenue(RevenueSearchCriteria criteria) throws SQLException;

    /** Thêm mới một bản ghi báo cáo doanh thu vào bảng DOANH_THU */
    void create(RevenueCreateRequest request) throws SQLException;

    /** Sinh mã doanh thu tiếp theo theo pattern DT-x */
    String generateNextId() throws SQLException;
}
