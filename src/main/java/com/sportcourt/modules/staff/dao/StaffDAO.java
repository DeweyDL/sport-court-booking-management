package com.sportcourt.modules.staff.dao;
import com.sportcourt.modules.staff.entity.Staff;
import com.sportcourt.modules.staff.entity.User;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import java.sql.Connection;
import java.util.List;


public class StaffDAO {

    public List<StaffResponse> search(StaffSearchCriteria criteria) {
        // SELECT danh sách nhân viên
        return null;
    }

    public StaffDetailResponse findDetailById(String maNv) {
        // SELECT chi tiết nhân viên
        return null;
    }

    public boolean existsByPhone(String sdt, String exceptUserId) {
        // kiểm tra trùng SĐT
        return false;
    }

    public boolean existsByEmail(String email, String exceptUserId) {
        // kiểm tra trùng email
        return false;
    }

    public boolean existsByCccd(String cccd, String exceptMaNv) {
        // kiểm tra trùng CCCD nếu cần
        return false;
    }

    public void insertUser(Connection conn, User user) {
        // INSERT INTO USERS
    }

    public void insertStaff(Connection conn, Staff staff) {
        // INSERT INTO NHAN_VIEN
    }

    public void updateUser(Connection conn, User user) {
        // UPDATE USERS
    }

    public void updateStaff(Connection conn, Staff staff) {
        // UPDATE NHAN_VIEN
    }

    public void softDeleteStaff(Connection conn, String maNv) {
        // UPDATE NHAN_VIEN SET IS_DELETED = 1 WHERE MANV = ?
    }
}