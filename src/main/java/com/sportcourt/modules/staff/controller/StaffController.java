package com.sportcourt.modules.staff.controller;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.service.StaffServiceImpl;
import com.sportcourt.modules.staff.view.StaffPanel;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        view.setRestoreAction(e -> restoreStaff());
        view.setRefreshAction(e -> searchStaff());
    }

    private void searchStaff() {
        StaffSearchCriteria criteria = view.getSearchCriteria();
        view.setLoading(true);

        SwingWorker<List<StaffResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StaffResponse> doInBackground() {
                return staffService.searchStaff(criteria);
            }

            @Override
            protected void done() {
                view.setLoading(false);
                try {
                    view.showStaffTable(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.showStaffTable(new ArrayList<>());
                    view.showError("Quá trình tải dữ liệu đã bị gián đoạn.");
                } catch (ExecutionException ex) {
                    view.showStaffTable(new ArrayList<>());
                    Throwable cause = ex.getCause();
                    view.showError(cause == null ? ex.getMessage() : cause.getMessage());
                }
            }
        };
        worker.execute();
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

            if (!view.confirm("Bạn có chắc muốn xoá mềm nhân viên này?")) {
                return;
            }

            staffService.deleteStaff(maNv);
            view.showMessage("Đã xóa nhân viên.");
            searchStaff();
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }

    private void restoreStaff() {
        try {
            String maNv = view.getSelectedStaffId();
            if (maNv == null) {
                view.showError("Vui lòng chọn nhân viên cần khôi phục.");
                return;
            }

            if (!view.confirm("Bạn có chắc muốn khôi phục nhân viên này?")) {
                return;
            }

            staffService.restoreStaff(maNv);
            view.showMessage("Khôi phục nhân viên thành công.");
            searchStaff();
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }
}
