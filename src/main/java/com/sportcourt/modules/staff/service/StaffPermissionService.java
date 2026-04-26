package com.sportcourt.modules.staff.service;
import com.sportcourt.modules.staff.dao.*;
import com.sportcourt.modules.staff.dto.*;
import com.sportcourt.modules.staff.entity.*;
import java.util.List;
public class StaffPermissionService {

    public void checkViewPermission() {
        if (!UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "VIEW")) {
            throw new RuntimeException("Bạn không có quyền xem danh sách nhân viên.");
        }
    }

    public void checkAddPermission() {
        if (!UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "ADD")) {
            throw new RuntimeException("Bạn không có quyền thêm nhân viên.");
        }
    }

    public void checkEditPermission() {
        if (!UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "EDIT")) {
            throw new RuntimeException("Bạn không có quyền cập nhật nhân viên.");
        }
    }

    public void checkDeletePermission() {
        if (!UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "DELETE")) {
            throw new RuntimeException("Bạn không có quyền xoá nhân viên.");
        }
    }

    public void checkBranchScope(String maCn) {
        UserSession session = UserSession.getInstance();

        if (!session.isOwner() && !session.getMaCn().equals(maCn)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên chi nhánh này.");
        }
    }
}