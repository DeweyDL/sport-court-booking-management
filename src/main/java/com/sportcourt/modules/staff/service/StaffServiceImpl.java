package com.sportcourt.modules.staff.service;

import com.sportcourt.modules.auth.dto.RoleGroupId;
import com.sportcourt.modules.auth.util.Sha256Password;
import com.sportcourt.modules.staff.dao.JdbcStaffDao;
import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.sql.SQLException;
import java.util.List;

public class StaffServiceImpl implements StaffService {
    private static final String DEFAULT_PASSWORD = "12345678";
    private static final String STAFF_ID_PREFIX = "NV-";
    private static final String STAFF_USER_PREFIX = "USERS-";
    private static final String STAFF_ACCOUNT_PREFIX = "ACC-";
    private static final String STAFF_ACCOUNT_ROLE_GROUP_PREFIX = "ACCRGR-";
    private static final String STAFF_ACCOUNT_ROLE_PREFIX = "ACCR-";
    private static final String MANAGER_ROLE_ID = "ROLE-25";
    private static final String CASHIER_ROLE_ID = "ROLE-43";
    private static final String MANAGER_STAFF_TYPE_ID = "LNV-2";
    private static final String CASHIER_STAFF_TYPE_ID = "LNV-3";

    private final JdbcStaffDao staffDao = new JdbcStaffDao();

    @Override
    public List<StaffResponse> searchStaff(StaffSearchCriteria criteria) throws Exception {
        String keyword = criteria != null ? criteria.getKeyword() : "";
        return staffDao.search(keyword);
    }

    @Override
    public void createStaff(StaffCreateRequest req) throws Exception {
        if (req.getHoten() == null || req.getHoten().trim().isEmpty()) {
            throw new Exception("Họ tên không được để trống!");
        }
        if (req.getSdt() == null || req.getSdt().trim().isEmpty()) {
            throw new Exception("Số điện thoại không được để trống!");
        }
        if (req.getCccd() == null || req.getCccd().trim().isEmpty()) {
            throw new Exception("Căn cước công dân không được để trống!");
        }

        int isQl = req.getIsQl();
        String roleGroupId = isQl == 1 ? RoleGroupId.BRANCH_MANAGER : RoleGroupId.CASHIER;
        String roleId = isQl == 1 ? MANAGER_ROLE_ID : CASHIER_ROLE_ID;
        String maLoaiNv = normalizeOptional(req.getMaLoaiNv());
        if (maLoaiNv == null) {
            maLoaiNv = isQl == 1 ? MANAGER_STAFF_TYPE_ID : CASHIER_STAFF_TYPE_ID;
        }

        String maNhanVien = normalizeOptional(req.getManv());
        if (maNhanVien == null) {
            maNhanVien = generateStaffId("NHAN_VIEN", "MANV", STAFF_ID_PREFIX);
        }

        String userId = generateStaffId("USERS", "USER_ID", STAFF_USER_PREFIX);
        String accountId = generateStaffId("ACCOUNT", "ACCOUNT_ID", STAFF_ACCOUNT_PREFIX);
        String accountRoleGroupId = generateStaffId(
                "ACCOUNT_ROLE_GROUP",
                "ACCOUNT_ROLE_GROUP_ID",
                STAFF_ACCOUNT_ROLE_GROUP_PREFIX
        );
        String accountRoleId = generateStaffId("ACCOUNT_ROLE", "ACCOUNT_ROLE_ID", STAFF_ACCOUNT_ROLE_PREFIX);
        String passwordHash = Sha256Password.hash(DEFAULT_PASSWORD);

        StaffCreateRequest normalized = new StaffCreateRequest();
        normalized.setManv(maNhanVien);
        normalized.setHoten(req.getHoten().trim());
        normalized.setSdt(req.getSdt().trim());
        normalized.setDiaChi(normalizeOptional(req.getDiaChi()));
        normalized.setCccd(req.getCccd().trim());
        normalized.setIsQl(isQl);
        normalized.setTrangThai(normalizeOptional(req.getTrangThai()));
        normalized.setMaCn(normalizeOptional(req.getMaCn()));
        normalized.setMaLoaiNv(maLoaiNv);

        try {
            boolean isSuccess = staffDao.insert(
                    userId,
                    accountId,
                    accountRoleGroupId,
                    accountRoleId,
                    roleGroupId,
                    roleId,
                    normalized,
                    maLoaiNv,
                    passwordHash
            );
            if (!isSuccess) {
                throw new Exception("Lỗi hệ thống: Không thể thêm nhân viên mới.");
            }
        } catch (SQLException e) {
            throw new Exception(extractOracleMessage(e.getMessage()));
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

        try {
            boolean isSuccess = staffDao.update(manv, req);
            if (!isSuccess) {
                throw new Exception("Lỗi hệ thống: Không thể cập nhật thông tin nhân viên.");
            }
        } catch (SQLException e) {
            throw new Exception(extractOracleMessage(e.getMessage()));
        }
    }

    @Override
    public void deleteStaff(String manv) throws Exception {
        if (manv == null || manv.trim().isEmpty()) {
            throw new Exception("Không xác định được nhân viên để xóa.");
        }

        try {
            boolean isSuccess = staffDao.delete(manv);
            if (!isSuccess) {
                throw new Exception("Lỗi hệ thống: Không thể xóa nhân viên.");
            }
        } catch (SQLException e) {
            throw new Exception(extractOracleMessage(e.getMessage()));
        }
    }

    @Override
    public void restoreStaff(String manv) throws Exception {
        if (manv == null || manv.trim().isEmpty()) {
            throw new Exception("Không xác định được nhân viên để khôi phục.");
        }

        try {
            boolean isSuccess = staffDao.restore(manv);
            if (!isSuccess) {
                throw new Exception("Lỗi hệ thống: Không thể khôi phục nhân viên.");
            }
        } catch (SQLException e) {
            throw new Exception(extractOracleMessage(e.getMessage()));
        }
    }

    @Override
    public String generateNextManv() throws Exception {
        return staffDao.generateNextManv();
    }

    @Override
    public List<String> loadBranchIds() throws Exception {
        return staffDao.loadBranchIds();
    }

    private String generateStaffId(String tableName, String idColumn, String prefix) throws SQLException {
        return staffDao.nextNumericId(tableName, idColumn, prefix);
    }

    private String extractOracleMessage(String raw) {
        if (raw == null) return "Lỗi hệ thống không xác định.";
        String firstLine = raw.split("\n")[0].trim();
        if (firstLine.matches("^ORA-\\d+:\\s*.*")) {
            return firstLine.replaceFirst("^ORA-\\d+:\\s*", "");
        }
        return firstLine;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
