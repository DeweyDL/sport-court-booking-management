package com.sportcourt.modules.staff.controller;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.service.StaffServiceImpl;
import com.sportcourt.modules.staff.view.StaffPanel;

public class StaffController {
    private final StaffPanel view;
    private final StaffService staffService;

    public StaffController(StaffPanel view) {
        this(view, new StaffServiceImpl());
    }

    public StaffController(StaffPanel view, StaffService staffService) {
        this.view = view;
        this.staffService = staffService;

        initEvents();
        searchStaff();
    }

    private void initEvents() {
        view.setSearchAction(e -> searchStaff());
        view.setAddAction(e -> addStaff());
        view.setUpdateAction(e -> updateStaff());
        view.setDeleteAction(e -> deleteStaff());
        view.setRefreshAction(e -> searchStaff());
    }

    private void searchStaff() {
        try {
            view.setLoading(true);

            StaffSearchCriteria criteria = view.getSearchCriteria();
            view.showStaffTable(staffService.searchStaff(criteria));
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể tải danh sách nhân viên."));
        } finally {
            view.setLoading(false);
        }
    }

    private void addStaff() {
        try {
            StaffCreateRequest request = view.showCreateDialog();

            if (request == null) {
                return;
            }

            staffService.createStaff(request);
            view.showMessage("Thêm nhân viên thành công.");
            searchStaff();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể thêm nhân viên."));
        }
    }

    private void updateStaff() {
        try {
            String maNv = view.getSelectedStaffId();

            if (maNv == null || maNv.trim().isEmpty()) {
                view.showError("Vui lòng chọn nhân viên cần chỉnh sửa.");
                return;
            }

            StaffDetailResponse detail = staffService.getStaffDetail(maNv);
            StaffUpdateRequest request = view.showUpdateDialog(detail);

            if (request == null) {
                return;
            }

            staffService.updateStaff(request);
            view.showMessage("Cập nhật nhân viên thành công.");
            searchStaff();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể cập nhật nhân viên."));
        }
    }

    private void deleteStaff() {
        try {
            String maNv = view.getSelectedStaffId();

            if (maNv == null || maNv.trim().isEmpty()) {
                view.showError("Vui lòng chọn nhân viên cần xoá.");
                return;
            }

            boolean confirmed = view.confirm("Bạn có chắc chắn muốn xoá nhân viên này không?");

            if (!confirmed) {
                return;
            }

            staffService.deleteStaff(maNv);
            view.showMessage("Xoá nhân viên thành công.");
            searchStaff();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể xoá nhân viên."));
        }
    }

    private String getErrorMessage(Exception e, String defaultMessage) {
        if (e == null) {
            return defaultMessage;
        }

        if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return e.getMessage();
        }

        Throwable cause = e.getCause();

        if (cause != null && cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
            return cause.getMessage();
        }

        return defaultMessage;
    }
}