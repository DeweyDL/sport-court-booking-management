package com.sportcourt.modules.staff.controller;

import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.view.StaffManagementView;

public class StaffController {
    private final StaffManagementView view;
    private final StaffService staffService;

    public StaffController(StaffManagementView view) {
        this.view = view;
        this.staffService = new StaffService();

        initActions();
        searchStaff();
    }

    private void initActions() {
        view.setSearchAction(e -> searchStaff());
        view.setAddAction(e -> addStaff());
        view.setUpdateAction(e -> updateStaff());
        view.setDeleteAction(e -> deleteStaff());
        view.setRestoreAction(e -> restoreStaff());
        view.setViewDetailAction(e -> viewDetailStaff());
        view.setRefreshAction(e -> searchStaff());
    }

    private void searchStaff() {
        try {
            StaffSearchCriteria criteria = view.getSearchCriteria();
            view.showStaffTable(staffService.searchStaff(criteria));
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void addStaff() {
        view.showCreateDialogWithHandler(request -> {
            staffService.createStaff(request);
            searchStaff();
            view.showMessage("Thêm nhân viên thành công.");
        });
    }

    private void updateStaff() {
        String maNv = view.getSelectedStaffId();

        if (maNv == null) {
            view.showError("Vui lòng chọn nhân viên cần cập nhật.");
            return;
        }

        try {
            StaffDetailResponse detail = staffService.getStaffDetail(maNv);

            view.showUpdateDialogWithHandler(detail, request -> {
                staffService.updateStaff(request);
                searchStaff();
                view.showMessage("Cập nhật nhân viên thành công.");
            });
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void deleteStaff() {
        String maNv = view.getSelectedStaffId();

        if (maNv == null) {
            view.showError("Vui lòng chọn nhân viên cần xoá.");
            return;
        }

        boolean confirmed = view.confirm("Bạn có chắc muốn xoá nhân viên này không?");

        if (!confirmed) {
            return;
        }

        try {
            staffService.deleteStaff(maNv);
            view.showMessage("Xoá nhân viên thành công.");
            searchStaff();
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void restoreStaff() {
        String maNv = view.getSelectedStaffId();

        if (maNv == null) {
            view.showError("Vui lòng chọn nhân viên cần khôi phục.");
            return;
        }

        boolean confirmed = view.confirm("Bạn có chắc muốn khôi phục nhân viên này không?");

        if (!confirmed) {
            return;
        }

        try {
            staffService.restoreStaff(maNv);
            view.showMessage("Khôi phục nhân viên thành công.");
            searchStaff();
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void viewDetailStaff() {
        String maNv = view.getSelectedStaffId();

        if (maNv == null) {
            view.showError("Vui lòng chọn nhân viên cần xem.");
            return;
        }

        try {
            StaffDetailResponse detail = staffService.getStaffDetail(maNv);
            view.showDetailDialog(detail);
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }
}
