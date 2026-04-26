package com.sportcourt.modules.staff.service;
import com.sportcourt.modules.staff.dao.*;
import com.sportcourt.modules.staff.dto.*;
import com.sportcourt.modules.staff.entity.*;
import java.util.List;

public class StaffService {
    private final StaffDAO staffDAO = new StaffDAO();
    private final StaffValidator validator = new StaffValidator();
    private final StaffPermissionService permissionService = new StaffPermissionService();

    public List<StaffResponse> searchStaff(StaffSearchCriteria criteria) {
        permissionService.checkViewPermission();

        if (!UserSession.getInstance().isOwner()) {
            criteria.setMaCn(UserSession.getInstance().getMaCn());
        }

        return staffDAO.search(criteria);
    }

    public StaffDetailResponse getStaffDetail(String maNv) {
        permissionService.checkViewPermission();

        StaffDetailResponse detail = staffDAO.findDetailById(maNv);
        if (detail == null) {
            throw new RuntimeException("Không tìm thấy nhân viên.");
        }

        permissionService.checkBranchScope(detail.getMaCn());

        return detail;
    }

    public void createStaff(StaffCreateRequest request) {
        permissionService.checkAddPermission();
        validator.validateCreate(request);
        permissionService.checkBranchScope(request.getMaCn());

        if (staffDAO.existsByPhone(request.getSdt(), null)) {
            throw new RuntimeException("Số điện thoại đã tồn tại.");
        }

        if (staffDAO.existsByEmail(request.getEmail(), null)) {
            throw new RuntimeException("Email đã tồn tại.");
        }

        if (staffDAO.existsByCccd(request.getCccd(), null)) {
            throw new RuntimeException("CCCD đã tồn tại.");
        }

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            User user = mapToUser(request);
            Staff staff = mapToStaff(request, user.getUserId());

            staffDAO.insertUser(conn, user);
            staffDAO.insertStaff(conn, staff);

            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Thêm nhân viên thất bại.", e);
        } finally {
            close(conn);
        }
    }

    public void updateStaff(StaffUpdateRequest request) {
        permissionService.checkEditPermission();
        validator.validateUpdate(request);

        StaffDetailResponse current = staffDAO.findDetailById(request.getMaNv());
        if (current == null) {
            throw new RuntimeException("Không tìm thấy nhân viên cần cập nhật.");
        }

        permissionService.checkBranchScope(current.getMaCn());
        permissionService.checkBranchScope(request.getMaCn());

        if (staffDAO.existsByPhone(request.getSdt(), current.getUserId())) {
            throw new RuntimeException("Số điện thoại đã tồn tại.");
        }

        if (staffDAO.existsByEmail(request.getEmail(), current.getUserId())) {
            throw new RuntimeException("Email đã tồn tại.");
        }

        if (staffDAO.existsByCccd(request.getCccd(), request.getMaNv())) {
            throw new RuntimeException("CCCD đã tồn tại.");
        }

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            User user = mapToUser(request);
            Staff staff = mapToStaff(request);

            staffDAO.updateUser(conn, user);
            staffDAO.updateStaff(conn, staff);

            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Cập nhật nhân viên thất bại.", e);
        } finally {
            close(conn);
        }
    }

    public void deleteStaff(String maNv) {
        permissionService.checkDeletePermission();

        StaffDetailResponse current = staffDAO.findDetailById(maNv);
        if (current == null) {
            throw new RuntimeException("Không tìm thấy nhân viên cần xoá.");
        }

        permissionService.checkBranchScope(current.getMaCn());

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            staffDAO.softDeleteStaff(conn, maNv);

            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Xoá nhân viên thất bại.", e);
        } finally {
            close(conn);
        }
    }

    private User mapToUser(StaffCreateRequest request) {
        User user = new User();
        user.setUserId(IdGenerator.nextUserId());
        user.setHoTen(request.getHoTen());
        user.setSdt(request.getSdt());
        user.setEmail(request.getEmail());
        user.setNgaySinh(request.getNgaySinh());
        user.setDiaChi(request.getDiaChi());
        return user;
    }

    private Staff mapToStaff(StaffCreateRequest request, String userId) {
        Staff staff = new Staff();
        staff.setMaNv(IdGenerator.nextStaffId());
        staff.setUserId(userId);
        staff.setMaCn(request.getMaCn());
        staff.setMaLoaiNv(request.getMaLoaiNv());
        staff.setNgayVaoLam(request.getNgayVaoLam());
        staff.setCccd(request.getCccd());
        staff.setQuanLy(request.isQuanLy());
        return staff;
    }

    private User mapToUser(StaffUpdateRequest request) {
        User user = new User();
        user.setUserId(request.getUserId());
        user.setHoTen(request.getHoTen());
        user.setSdt(request.getSdt());
        user.setEmail(request.getEmail());
        user.setNgaySinh(request.getNgaySinh());
        user.setDiaChi(request.getDiaChi());
        return user;
    }

    private Staff mapToStaff(StaffUpdateRequest request) {
        Staff staff = new Staff();
        staff.setMaNv(request.getMaNv());
        staff.setUserId(request.getUserId());
        staff.setMaCn(request.getMaCn());
        staff.setMaLoaiNv(request.getMaLoaiNv());
        staff.setNgayVaoLam(request.getNgayVaoLam());
        staff.setCccd(request.getCccd());
        staff.setQuanLy(request.isQuanLy());
        return staff;
    }
}