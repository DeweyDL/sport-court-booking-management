package com.sportcourt.modules.staff.controller;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.view.StaffManagementView;

import java.util.ArrayList;
import java.util.List;

public class StaffController {
    private final StaffManagementView view;
    private final StaffService staffService;

    public StaffController(StaffManagementView view) {
        this.view = view;
        this.staffService = new StaffService();

        initEvents();

        // Không tự load DB khi vừa mở màn hình.
        // Tránh lỗi popup DB làm đứng giao diện trước khi bấm thêm nhân viên.
        view.showStaffTable(new ArrayList<>());
    }

    private void initEvents() {
        view.setSearchAction(e -> searchStaff());
        view.setAddAction(e -> addStaff());
        view.setUpdateAction(e -> updateStaff());
        view.setDeleteAction(e -> deleteStaff());
        view.setRefreshAction(e -> searchStaff());
        view.setViewDetailAction(e -> viewDetail());
    }

    private void searchStaff() {
        try {
            StaffSearchCriteria criteria = view.getSearchCriteria();
            List<StaffResponse> result = staffService.searchStaff(criteria);
            view.showStaffTable(result);
        } catch (Exception ex) {
            view.showStaffTable(new ArrayList<>());
            view.showError(ex.getMessage());
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
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void updateStaff() {
        try {
            String maNv = view.getSelectedStaffId();

            if (maNv == null) {
                view.showError("Vui lòng chọn nhân viên cần cập nhật.");
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
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void deleteStaff() {
        try {
            String maNv = view.getSelectedStaffId();

            if (maNv == null) {
                view.showError("Vui lòng chọn nhân viên cần xoá.");
                return;
            }

            if (!view.confirm("Bạn có chắc muốn xoá nhân viên này?")) {
                return;
            }

            staffService.deleteStaff(maNv);
            view.showMessage("Xoá nhân viên thành công.");
            searchStaff();
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void viewDetail() {
        try {
            String maNv = view.getSelectedStaffId();

            if (maNv == null) {
                view.showError("Vui lòng chọn nhân viên cần xem chi tiết.");
                return;
            }

            StaffDetailResponse detail = staffService.getStaffDetail(maNv);
            view.showDetailDialog(detail);
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }
}