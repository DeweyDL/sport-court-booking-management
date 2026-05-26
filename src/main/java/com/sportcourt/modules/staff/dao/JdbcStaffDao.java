package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcStaffDao {

    // 1. Tìm kiếm và hiển thị danh sách (bao gồm cả đã xóa)
    public List<StaffResponse> search(String keyword) {
        List<StaffResponse> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT nv.MANV, u.HOTEN, u.SDT, u.DIACHI, nv.CCCD, nv.IS_QL, nv.TRANG_THAI, nv.NVL, nv.MACN, nv.IS_DELETED " +
                        "FROM NHAN_VIEN nv " +
                        "JOIN USERS u ON nv.USER_ID = u.USER_ID "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("WHERE (LOWER(nv.MANV) LIKE ? OR LOWER(u.HOTEN) LIKE ? OR nv.CCCD LIKE ?) ");
        }

        sql.append("ORDER BY nv.IS_DELETED ASC, nv.MANV ASC");

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.toLowerCase().trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
                ps.setString(3, "%" + keyword.trim() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffResponse dto = new StaffResponse();
                    dto.setManv(rs.getString("MANV"));
                    dto.setHoten(rs.getString("HOTEN"));
                    dto.setSdt(rs.getString("SDT"));
                    dto.setDiaChi(rs.getString("DIACHI"));
                    dto.setCccd(rs.getString("CCCD"));
                    dto.setIsQl(rs.getInt("IS_QL"));
                    dto.setChucVu(rs.getInt("IS_QL") == 1 ? "Quản lý" : "Nhân viên");
                    dto.setTrangThai(rs.getString("TRANG_THAI"));
                    dto.setMaCn(rs.getString("MACN"));
                    dto.setDeleted(rs.getInt("IS_DELETED") == 1);
                    if (rs.getDate("NVL") != null) {
                        dto.setNgayVaoLam(rs.getDate("NVL").toLocalDate());
                    }
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm nhân viên mới qua procedure PRC_THEM_NHAN_VIEN
    public String nextNumericId(String tableName, String idColumn, String prefix) throws SQLException {
        String sql = switch ((tableName + "." + idColumn).toUpperCase()) {
            case "USERS.USER_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(USER_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM USERS
                    WHERE USER_ID LIKE ?
                    """;
            case "ACCOUNT.ACCOUNT_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCOUNT_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM ACCOUNT
                    WHERE ACCOUNT_ID LIKE ?
                    """;
            case "ACCOUNT_ROLE_GROUP.ACCOUNT_ROLE_GROUP_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCOUNT_ROLE_GROUP_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM ACCOUNT_ROLE_GROUP
                    WHERE ACCOUNT_ROLE_GROUP_ID LIKE ?
                    """;
            case "ACCOUNT_ROLE.ACCOUNT_ROLE_ID" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(ACCOUNT_ROLE_ID, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM ACCOUNT_ROLE
                    WHERE ACCOUNT_ROLE_ID LIKE ?
                    """;
            case "NHAN_VIEN.MANV" -> """
                    SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MANV, '\\d+$'))), 0) + 1 AS NEXT_ID
                    FROM NHAN_VIEN
                    WHERE MANV LIKE ?
                    """;
            default -> throw new SQLException("Khong ho tro sinh ID cho " + tableName + "." + idColumn);
        };

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return prefix + rs.getLong("NEXT_ID");
                }
            }
        }
        throw new SQLException("Khong the sinh ID moi cho " + tableName + "." + idColumn);
    }

    public boolean insert(String userId,
                          String accountId,
                          String accountRoleGroupId,
                          String accountRoleId,
                          String roleGroupId,
                          String roleId,
                          StaffCreateRequest req,
                          String maLoaiNv,
                          String passwordHash) throws SQLException {
        String call = "{ call PRC_THEM_NHAN_VIEN(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        String insertAccountRoleSql = """
                INSERT INTO ACCOUNT_ROLE (ACCOUNT_ROLE_ID, ACCOUNT_ID, ROLE_ID, CREATED_AT, IS_DELETED)
                VALUES (?, ?, ?, SYSDATE, 0)
                """;
        String updateTrangThaiSql = "UPDATE NHAN_VIEN SET TRANG_THAI = ? WHERE MANV = ?";
        String updateDiaChiSql = "UPDATE USERS SET DIACHI = ? WHERE USER_ID = ?";

        String manv = normalizeOptional(req.getManv());
        String trangThai = normalizeOptional(req.getTrangThai());
        String diaChi = normalizeOptional(req.getDiaChi());

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (CallableStatement ps = conn.prepareCall(call)) {
                    ps.setString(1, userId);
                    ps.setString(2, manv);
                    ps.setString(3, accountId);
                    ps.setString(4, accountRoleGroupId);
                    ps.setString(5, normalizeOptional(req.getHoten()));
                    ps.setString(6, normalizeOptional(req.getSdt()));
                    ps.setString(7, null);
                    ps.setString(8, passwordHash);
                    ps.setString(9, normalizeOptional(req.getMaCn()));
                    ps.setString(10, maLoaiNv);
                    ps.setString(11, normalizeOptional(req.getCccd()));
                    ps.setInt(12, req.getIsQl());
                    ps.execute();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertAccountRoleSql)) {
                    ps.setString(1, accountRoleId);
                    ps.setString(2, accountId);
                    ps.setString(3, roleId);
                    ps.executeUpdate();
                }

                if (trangThai != null) {
                    try (PreparedStatement ps = conn.prepareStatement(updateTrangThaiSql)) {
                        ps.setString(1, trangThai);
                        ps.setString(2, manv);
                        ps.executeUpdate();
                    }
                }

                if (diaChi != null) {
                    try (PreparedStatement ps = conn.prepareStatement(updateDiaChiSql)) {
                        ps.setString(1, diaChi);
                        ps.setString(2, userId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // 3. Sửa thông tin nhân viên
    public boolean update(String manv, StaffUpdateRequest req) throws SQLException {
        String sqlFindUserId = "SELECT USER_ID FROM NHAN_VIEN WHERE MANV = ?";
        String sqlUpdateUser = "UPDATE USERS SET HOTEN = ?, SDT = ?, DIACHI = ? WHERE USER_ID = ?";
        String sqlUpdateStaff = "UPDATE NHAN_VIEN SET CCCD = ?, IS_QL = ?, TRANG_THAI = ?, MACN = ? WHERE MANV = ?";

        String cccd = req.getCccd();
        if (cccd != null && cccd.trim().isEmpty()) {
            cccd = null;
        }
        String sdt = (req.getSdt() != null && !req.getSdt().trim().isEmpty()) ? req.getSdt().trim() : null;
        String diaChi = (req.getDiaChi() != null && !req.getDiaChi().trim().isEmpty()) ? req.getDiaChi().trim() : null;

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement psFind = conn.prepareStatement(sqlFindUserId);
             PreparedStatement psUser = conn.prepareStatement(sqlUpdateUser);
             PreparedStatement psStaff = conn.prepareStatement(sqlUpdateStaff)) {

            psFind.setString(1, manv);
            ResultSet rs = psFind.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("USER_ID");

                psUser.setString(1, req.getHoten());
                psUser.setString(2, sdt);
                psUser.setString(3, diaChi);
                psUser.setString(4, userId);
                psUser.executeUpdate();

                psStaff.setString(1, cccd);
                psStaff.setInt(2, req.getIsQl());
                psStaff.setString(3, req.getTrangThai());
                psStaff.setString(4, req.getMaCn() != null && !req.getMaCn().trim().isEmpty() ? req.getMaCn().trim() : null);
                psStaff.setString(5, manv);
                return psStaff.executeUpdate() > 0;
            }
            return false;
        }
    }

    public List<String> loadBranchIds() {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT MACN FROM CHI_NHANH WHERE IS_DELETED = 0 ORDER BY MACN ASC";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getString("MACN"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public String generateNextManv() throws SQLException {
        return nextNumericId("NHAN_VIEN", "MANV", "NV-");
    }

    // 4. Xóa mềm nhân viên qua procedure PRC_XOA_NHAN_VIEN
    public boolean delete(String manv) throws SQLException {
        String call = "{ call PRC_XOA_NHAN_VIEN(?) }";
        try (Connection conn = ConnectionUtils.getMyConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, manv);
            cs.execute();
            return true;
        }
    }

    // 5. Khôi phục nhân viên đã xóa (khôi phục cả USERS, ACCOUNT, ACCOUNT_ROLE_GROUP, ACCOUNT_ROLE)
    public boolean restore(String manv) throws SQLException {
        String restoreStaff = "UPDATE NHAN_VIEN SET IS_DELETED = 0, TRANG_THAI = 'ACTIVE' WHERE MANV = ?";
        String restoreUser = """
                UPDATE USERS SET IS_DELETED = 0
                WHERE USER_ID = (SELECT USER_ID FROM NHAN_VIEN WHERE MANV = ?)
                """;
        String restoreAccount = """
                UPDATE ACCOUNT SET STATUS = 'ACTIVE', IS_DELETED = 0
                WHERE USER_ID = (SELECT USER_ID FROM NHAN_VIEN WHERE MANV = ?)
                """;
        String restoreRoleGroup = """
                UPDATE ACCOUNT_ROLE_GROUP SET IS_DELETED = 0
                WHERE ACCOUNT_ID = (
                    SELECT a.ACCOUNT_ID FROM ACCOUNT a
                    JOIN NHAN_VIEN nv ON nv.USER_ID = a.USER_ID
                    WHERE nv.MANV = ?
                )
                """;
        String restoreRole = """
                UPDATE ACCOUNT_ROLE SET IS_DELETED = 0
                WHERE ACCOUNT_ID = (
                    SELECT a.ACCOUNT_ID FROM ACCOUNT a
                    JOIN NHAN_VIEN nv ON nv.USER_ID = a.USER_ID
                    WHERE nv.MANV = ?
                )
                """;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psStaff = conn.prepareStatement(restoreStaff);
                 PreparedStatement psUser = conn.prepareStatement(restoreUser);
                 PreparedStatement psAccount = conn.prepareStatement(restoreAccount);
                 PreparedStatement psRoleGroup = conn.prepareStatement(restoreRoleGroup);
                 PreparedStatement psRole = conn.prepareStatement(restoreRole)) {

                psStaff.setString(1, manv);
                int updated = psStaff.executeUpdate();
                if (updated <= 0) {
                    conn.rollback();
                    return false;
                }

                psUser.setString(1, manv);
                psUser.executeUpdate();

                psAccount.setString(1, manv);
                psAccount.executeUpdate();

                psRoleGroup.setString(1, manv);
                psRoleGroup.executeUpdate();

                psRole.setString(1, manv);
                psRole.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
