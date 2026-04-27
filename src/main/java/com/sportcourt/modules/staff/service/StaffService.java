package com.sportcourt.modules.staff.service;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.dao.StaffAccountDAO;
import com.sportcourt.modules.staff.dao.StaffDAO;
import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.entity.Staff;
import com.sportcourt.modules.staff.entity.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StaffService {
    private static final AtomicInteger USER_COUNTER = new AtomicInteger(1);
    private static final AtomicInteger ACCOUNT_COUNTER = new AtomicInteger(1);
    private static final AtomicInteger ACCOUNT_GROUP_COUNTER = new AtomicInteger(1);

    private final StaffDAO staffDAO;
    private final StaffAccountDAO staffAccountDAO;
    private final StaffValidator validator;
    private final StaffPermissionService permissionService;

    public StaffService() {
        this.staffDAO = new StaffDAO();
        this.staffAccountDAO = new StaffAccountDAO();
        this.validator = new StaffValidator();
        this.permissionService = new StaffPermissionService();
    }

    public List<StaffResponse> searchStaff(StaffSearchCriteria criteria) {
        permissionService.checkViewPermission();

        if (criteria == null) {
            criteria = new StaffSearchCriteria();
        }

        List<StaffResponse> result = staffDAO.search(criteria);
        return result == null ? new ArrayList<>() : result;
    }

    public StaffDetailResponse getStaffDetail(String maNv) {
        permissionService.checkViewPermission();

        if (isBlank(maNv)) {
            throw new RuntimeException("Vui lòng chọn nhân viên cần xem.");
        }

        StaffDetailResponse detail = staffDAO.findDetailById(maNv);

        if (detail == null) {
            throw new RuntimeException("Không tìm thấy nhân viên.");
        }

        return detail;
    }

    public void createStaff(StaffCreateRequest request) {
        permissionService.checkAddPermission();
        validator.validateCreate(request);

        if (staffDAO.existsByPhone(request.getSdt(), null)) {
            throw new RuntimeException("Số điện thoại đã tồn tại.");
        }

        if (staffDAO.existsByEmail(request.getEmail(), null)) {
            throw new RuntimeException("Email đã tồn tại.");
        }

        if (staffDAO.existsByCccd(request.getCccd(), null)) {
            throw new RuntimeException("Căn cước công dân đã tồn tại.");
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            User user = mapToUser(request);
            Staff staff = mapToStaff(request, user.getUserId(), conn);

            staffDAO.insertUser(conn, user);
            staffDAO.insertStaff(conn, staff);

            if (request.isCreateAccount()) {
                String accountId = nextAccountId();
                String accountRoleGroupId = nextAccountRoleGroupId();

                staffAccountDAO.insertAccount(
                        conn,
                        accountId,
                        user.getUserId(),
                        request.getUsername(),
                        hashPassword(request.getPassword())
                );

                staffAccountDAO.assignRoleGroup(
                        conn,
                        accountRoleGroupId,
                        accountId,
                        request.getRoleGroupId()
                );
            }

            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Thêm nhân viên thất bại: " + e.getMessage(), e);
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

        if (staffDAO.existsByPhone(request.getSdt(), current.getUserId())) {
            throw new RuntimeException("Số điện thoại đã tồn tại.");
        }

        if (staffDAO.existsByEmail(request.getEmail(), current.getUserId())) {
            throw new RuntimeException("Email đã tồn tại.");
        }

        if (staffDAO.existsByCccd(request.getCccd(), request.getMaNv())) {
            throw new RuntimeException("Căn cước công dân đã tồn tại.");
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            User user = mapToUser(request);
            Staff staff = mapToStaff(request);

            staffDAO.updateUser(conn, user);
            staffDAO.updateStaff(conn, staff);

            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Cập nhật nhân viên thất bại: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    public void deleteStaff(String maNv) {
        permissionService.checkDeletePermission();

        if (isBlank(maNv)) {
            throw new RuntimeException("Vui lòng chọn nhân viên cần xoá.");
        }

        StaffDetailResponse current = staffDAO.findDetailById(maNv);

        if (current == null) {
            throw new RuntimeException("Không tìm thấy nhân viên cần xoá.");
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            staffDAO.softDeleteStaff(conn, maNv);
            staffAccountDAO.lockAccountByUserId(conn, current.getUserId());

            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Xoá nhân viên thất bại: " + e.getMessage(), e);
        } finally {
            close(conn);
        }
    }

    private User mapToUser(StaffCreateRequest request) {
        User user = new User();

        user.setUserId(nextUserId());
        user.setHoTen(request.getHoTen());
        user.setSdt(request.getSdt());
        user.setEmail(request.getEmail());
        user.setNgaySinh(request.getNgaySinh());
        user.setDiaChi(request.getDiaChi());
        user.setDeleted(false);

        return user;
    }

    private Staff mapToStaff(StaffCreateRequest request, String userId, Connection conn) {
        Staff staff = new Staff();

        staff.setMaNv(staffDAO.nextStaffId(conn));
        staff.setUserId(userId);

        // Lấy đúng mã loại nhân viên do người dùng nhập ở form.
        staff.setMaLoaiNv(request.getMaLoaiNv());

        staff.setNgayVaoLam(request.getNgayVaoLam());
        staff.setCccd(request.getCccd());
        staff.setQuanLy(request.isQuanLy());
        staff.setDeleted(false);

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

        // Lấy đúng mã loại nhân viên do người dùng nhập ở form cập nhật.
        staff.setMaLoaiNv(request.getMaLoaiNv());

        staff.setNgayVaoLam(request.getNgayVaoLam());
        staff.setCccd(request.getCccd());
        staff.setQuanLy(request.isQuanLy());

        return staff;
    }

    private String nextUserId() {
        long time = System.currentTimeMillis();
        return "U" + time + String.format("%03d", USER_COUNTER.getAndIncrement());
    }

    private String nextAccountId() {
        long time = System.currentTimeMillis();
        return "ACC" + time + String.format("%03d", ACCOUNT_COUNTER.getAndIncrement());
    }

    private String nextAccountRoleGroupId() {
        long time = System.currentTimeMillis();
        return "ARG" + time + String.format("%03d", ACCOUNT_GROUP_COUNTER.getAndIncrement());
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();

            for (byte b : encoded) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể mã hóa mật khẩu.", e);
        }
    }

    private void rollback(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void close(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }

        ConnectionUtils.close(conn, null, null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}