package com.sportcourt.modules.staff.service;

public class StaffPermissionService {
    private static final String FUNCTION_CODE = "STAFF_MANAGEMENT";

    public boolean canView() {
        return hasPermission(FUNCTION_CODE, "VIEW");
    }

    public boolean canAdd() {
        return hasPermission(FUNCTION_CODE, "ADD");
    }

    public boolean canEdit() {
        return hasPermission(FUNCTION_CODE, "EDIT");
    }

    public boolean canDelete() {
        return hasPermission(FUNCTION_CODE, "DELETE");
    }

    public void checkViewPermission() {
        if (!canView()) {
            throw new RuntimeException("Bạn không có quyền xem danh sách nhân viên.");
        }
    }

    public void checkAddPermission() {
        if (!canAdd()) {
            throw new RuntimeException("Bạn không có quyền thêm nhân viên.");
        }
    }

    public void checkEditPermission() {
        if (!canEdit()) {
            throw new RuntimeException("Bạn không có quyền cập nhật nhân viên.");
        }
    }

    public void checkDeletePermission() {
        if (!canDelete()) {
            throw new RuntimeException("Bạn không có quyền xoá nhân viên.");
        }
    }

    public void checkBranchScope(String maCn) {
        // Bản tạm để module staff chạy độc lập.
        // Khi UserSession của auth ổn định, thay bằng kiểm tra chi nhánh thật.
    }

    public boolean isOwner() {
        return true;
    }

    public String getCurrentBranchCode() {
        return null;
    }

    private boolean hasPermission(String functionCode, String action) {
        return true;
    }
}
