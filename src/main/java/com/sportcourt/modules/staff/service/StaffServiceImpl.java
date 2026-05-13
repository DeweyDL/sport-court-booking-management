package com.sportcourt.modules.staff.service;

import com.sportcourt.modules.staff.dao.JdbcStaffDao;
import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.util.List;

public class StaffServiceImpl implements StaffService {

    private final JdbcStaffDao staffDao = new JdbcStaffDao();

    @Override
    public List<StaffResponse> searchStaff(StaffSearchCriteria criteria) throws Exception {
        String keyword = criteria != null ? criteria.getKeyword() : "";
        return staffDao.search(keyword);
    }

    @Override
    public void createStaff(StaffCreateRequest req) throws Exception {
        if (req.getManv() == null || req.getManv().trim().isEmpty()) {
            throw new Exception("Mã nhân viên không được để trống!");
        }
        if (req.getHoten() == null || req.getHoten().trim().isEmpty()) {
            throw new Exception("Họ tên không được để trống!");
        }

        boolean isSuccess = staffDao.insert(req);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể thêm nhân viên mới.");
        }
    }

    @Override
    public void updateStaff(String manv, StaffUpdateRequest req) throws Exception {
        if (manv == null || manv.trim().isEmpty()) {
            throw new Exception("Không xác định được mã nhân viên để cập nhật.");
        }
        if (req.getHoten() == null || req.getHoten().trim().isEmpty()) {
            throw new Exception("Họ tên không được để trống!");
        }

        boolean isSuccess = staffDao.update(manv, req);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể cập nhật thông tin nhân viên.");
        }
    }

    @Override
    public void deleteStaff(String manv) throws Exception {
        if (manv == null || manv.trim().isEmpty()) {
            throw new Exception("Không xác định được nhân viên để xóa.");
        }

        boolean isSuccess = staffDao.delete(manv);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể xóa nhân viên.");
        }
    }

    @Override
    public void restoreStaff(String manv) throws Exception {
        if (manv == null || manv.trim().isEmpty()) {
            throw new Exception("Không xác định được nhân viên để khôi phục.");
        }

        boolean isSuccess = staffDao.restore(manv);
        if (!isSuccess) {
            throw new Exception("Lỗi hệ thống: Không thể khôi phục nhân viên.");
        }
    }

    @Override
    public String generateNextManv() throws Exception {
        return staffDao.generateNextManv();
    }
}