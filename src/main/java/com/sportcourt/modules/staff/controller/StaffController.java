package com.sportcourt.modules.staff.controller;

import com.sportcourt.modules.staff.service.StaffService;
import com.sportcourt.modules.staff.service.StaffServiceImpl;
import com.sportcourt.modules.staff.view.StaffPanel;

/**
 * StaffController — cầu nối giữa StaffPanel và StaffService.
 *
 * Kiến trúc mới: StaffPanel tự quản lý sự kiện (search, add, edit, delete)
 * bên trong nên Controller chỉ giữ tham chiếu để hỗ trợ các luồng bên ngoài
 * (ví dụ: StaffModuleRunner khởi tạo panel qua controller).
 */
public class StaffController {

    private final StaffPanel   view;
    private final StaffService staffService;

    /** Constructor chính — dùng trong StaffModuleRunner. */
    public StaffController(StaffPanel view) {
        this(view, new StaffServiceImpl());
    }

    /** Constructor đầy đủ — dùng khi cần inject service tùy chỉnh (test). */
    public StaffController(StaffPanel view, StaffService staffService) {
        this.view         = view;
        this.staffService = staffService;
    }

    /**
     * Tải/làm mới danh sách nhân viên.
     * Gọi thẳng vào StaffPanel.loadData() để tái sử dụng logic search + render đã có.
     */
    public void refresh() {
        view.loadData();
    }

    /** Trả về panel để embed vào màn hình chính. */
    public StaffPanel getView() {
        return view;
    }
}