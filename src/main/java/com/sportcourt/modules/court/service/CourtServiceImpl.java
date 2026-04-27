package com.sportcourt.modules.court.service;


import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.court.dao.CourtDAO;
import com.sportcourt.modules.court.dao.CourtDAOImpl;
import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class CourtServiceImpl implements CourtService {
    private final CourtDAO courtDAO;

    public CourtServiceImpl() {
        this(new CourtDAOImpl());
    }

    public CourtServiceImpl(CourtDAO courtDAO) {
        this.courtDAO = courtDAO;
    }

    @Override
    public List<CourtTableRow> search(CourtSearchCriteria criteria) throws SQLException {
        if (criteria == null) {
            throw new IllegalArgumentException("Điều kiện tìm kiếm không được rỗng.");
        }

        if (isBlank(criteria.getBranchId())) {
            throw new IllegalArgumentException("Không xác định được chi nhánh hiện tại.");
        }

        return courtDAO.findByCriteria(criteria);
    }

    @Override
    public Optional<CourtTableRow> findDetail(String courtId, String branchId) throws SQLException {
        if (isBlank(courtId)) {
            throw new IllegalArgumentException("Vui lòng chọn sân con cần xem chi tiết.");
        }

        if (isBlank(branchId)) {
            throw new IllegalArgumentException("Không xác định được chi nhánh hiện tại.");
        }

        return courtDAO.findDetail(courtId, branchId);
    }

    @Override
    public void create(Court court, String branchId) throws SQLException {
        validateCourtRequiredFields(court);

        if (isBlank(branchId)) {
            throw new IllegalArgumentException("Không xác định được chi nhánh hiện tại.");
        }

        if (courtDAO.existsById(court.getCourtId())) {
            throw new IllegalArgumentException("Mã sân con đã tồn tại.");
        }

        if (!courtDAO.areaBelongsToBranch(court.getAreaId(), branchId)) {
            throw new IllegalArgumentException("Khu vực không thuộc chi nhánh hiện tại.");
        }

        courtDAO.insert(court);
    }

    @Override
    public void update(Court court, String branchId) throws SQLException {
        validateCourtRequiredFields(court);

        if (isBlank(branchId)) {
            throw new IllegalArgumentException("Không xác định được chi nhánh hiện tại.");
        }

        if (courtDAO.findByIdInBranch(court.getCourtId(), branchId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sân con thuộc chi nhánh hiện tại.");
        }

        if (!courtDAO.areaBelongsToBranch(court.getAreaId(), branchId)) {
            throw new IllegalArgumentException("Khu vực mới không thuộc chi nhánh hiện tại.");
        }

        boolean updated = courtDAO.update(court, branchId);

        if (!updated) {
            throw new IllegalStateException("Cập nhật sân con thất bại.");
        }
    }

    @Override
    public void delete(String courtId, String branchId) throws SQLException {
        if (isBlank(courtId)) {
            throw new IllegalArgumentException("Vui lòng chọn sân con cần xóa.");
        }

        if (isBlank(branchId)) {
            throw new IllegalArgumentException("Không xác định được chi nhánh hiện tại.");
        }

        if (courtDAO.findByIdInBranch(courtId, branchId).isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy sân con thuộc chi nhánh hiện tại.");
        }

        if (courtDAO.hasActiveRental(courtId, branchId)) {
            throw new IllegalArgumentException("Sân con đang có lịch thuê còn hiệu lực, không thể xóa.");
        }

        boolean deleted = courtDAO.softDelete(courtId, branchId);

        if (!deleted) {
            throw new IllegalStateException("Xóa sân con thất bại.");
        }
    }

    private void validateCourtRequiredFields(Court court) {
        if (court == null) {
            throw new IllegalArgumentException("Thông tin sân con không được rỗng.");
        }

        if (isBlank(court.getCourtId())) {
            throw new IllegalArgumentException("Mã sân con không được để trống.");
        }

        if (isBlank(court.getAreaId())) {
            throw new IllegalArgumentException("Vui lòng chọn khu vực.");
        }

        if (isBlank(court.getStatus())) {
            throw new IllegalArgumentException("Vui lòng chọn trạng thái sân.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
