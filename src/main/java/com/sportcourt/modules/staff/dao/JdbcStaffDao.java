package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JdbcStaffDao {

    // 1. Tìm kiếm và hiển thị danh sách
    public List<StaffResponse> search(String keyword) {
        List<StaffResponse> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT nv.MANV, u.HOTEN, nv.CCCD, nv.IS_QL, nv.TRANG_THAI " +
                        "FROM NHAN_VIEN nv " +
                        "JOIN USERS u ON nv.USER_ID = u.USER_ID " +
                        "WHERE nv.IS_DELETED = 0 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(nv.MANV) LIKE ? OR LOWER(u.HOTEN) LIKE ? OR nv.CCCD LIKE ?) ");
        }

        sql.append("ORDER BY nv.MANV ASC");

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
                    dto.setCccd(rs.getString("CCCD"));
                    dto.setIsQl(rs.getInt("IS_QL"));
                    dto.setChucVu(rs.getInt("IS_QL") == 1 ? "Quản lý" : "Nhân viên");
                    dto.setTrangThai(rs.getString("TRANG_THAI"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm nhân viên mới (Đã chuẩn hóa hoàn toàn theo DDL)
    public boolean insert(StaffCreateRequest req) throws SQLException {
        Connection conn = null;
        PreparedStatement psUser = null;
        PreparedStatement psStaff = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false); // Bật Transaction

            // Sinh mã USER_ID ngẫu nhiên
            String userId = "USR_" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            // Insert bảng USERS (SDT là bắt buộc, gán giá trị thỏa mãn regex ^0[0-9]{9}$)
            String sqlUser = "INSERT INTO USERS (USER_ID, HOTEN, SDT) VALUES (?, ?, '0999999999')";
            psUser = conn.prepareStatement(sqlUser);
            psUser.setString(1, userId);
            psUser.setString(2, req.getHoten());
            psUser.executeUpdate();

            // --- TỰ ĐỘNG TÌM MACN TRONG DB ---
            String defaultMacn = null;
            try(PreparedStatement ps1 = conn.prepareStatement("SELECT MACN FROM CHI_NHANH WHERE IS_DELETED = 0 AND ROWNUM = 1");
                ResultSet rs1 = ps1.executeQuery()) {
                if(rs1.next()) defaultMacn = rs1.getString("MACN");
            }

            // NẾU DB TRỐNG CHI NHÁNH -> TỰ INSERT ĐÚNG CẤU TRÚC (Có đủ MACN, TEN_CHI_NHANH, DIACHI)
            if (defaultMacn == null) {
                defaultMacn = "CN01";
                try (PreparedStatement psCN = conn.prepareStatement("INSERT INTO CHI_NHANH (MACN, TEN_CHI_NHANH, DIACHI) VALUES (?, ?, ?)")) {
                    psCN.setString(1, defaultMacn);
                    psCN.setString(2, "Chi nhánh mặc định");
                    psCN.setString(3, "Chưa cập nhật địa chỉ");
                    psCN.executeUpdate();
                }
            }

            // --- TỰ ĐỘNG TÌM MALNV TRONG DB ---
            String defaultMalnv = null;
            try(PreparedStatement ps2 = conn.prepareStatement("SELECT MALNV FROM LOAI_NHAN_VIEN WHERE IS_DELETED = 0 AND ROWNUM = 1");
                ResultSet rs2 = ps2.executeQuery()) {
                if(rs2.next()) defaultMalnv = rs2.getString("MALNV");
            }

            // NẾU DB TRỐNG LOẠI NV -> TỰ INSERT ĐÚNG CẤU TRÚC (Có đủ MALNV, VITRI, MUC_LUONG)
            if (defaultMalnv == null) {
                defaultMalnv = "LNV01";
                try (PreparedStatement psLNV = conn.prepareStatement("INSERT INTO LOAI_NHAN_VIEN (MALNV, VITRI, MUC_LUONG) VALUES (?, ?, ?)")) {
                    psLNV.setString(1, defaultMalnv);
                    psLNV.setString(2, "Nhân viên mặc định");
                    psLNV.setDouble(3, 0.0);
                    psLNV.executeUpdate();
                }
            }

            // Xử lý CCCD rỗng thành NULL để không bị dính chưởng Constraint Regex
            String cccd = req.getCccd();
            if (cccd != null && cccd.trim().isEmpty()) {
                cccd = null;
            }

            // Insert NHAN_VIEN
            String sqlStaff = "INSERT INTO NHAN_VIEN (MANV, USER_ID, MALNV, MACN, NVL, CCCD, IS_QL, TRANG_THAI, IS_DELETED) " +
                    "VALUES (?, ?, ?, ?, SYSDATE, ?, ?, ?, 0)";

            psStaff = conn.prepareStatement(sqlStaff);
            psStaff.setString(1, req.getManv());
            psStaff.setString(2, userId);
            psStaff.setString(3, defaultMalnv);
            psStaff.setString(4, defaultMacn);
            psStaff.setString(5, cccd);
            psStaff.setInt(6, req.getIsQl());
            psStaff.setString(7, req.getTrangThai());

            psStaff.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (psUser != null) psUser.close();
            if (psStaff != null) psStaff.close();
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    // 3. Sửa thông tin nhân viên
    public boolean update(String manv, StaffUpdateRequest req) throws SQLException {
        String sqlFindUserId = "SELECT USER_ID FROM NHAN_VIEN WHERE MANV = ?";
        String sqlUpdateUser = "UPDATE USERS SET HOTEN = ? WHERE USER_ID = ?";
        String sqlUpdateStaff = "UPDATE NHAN_VIEN SET CCCD = ?, IS_QL = ?, TRANG_THAI = ? WHERE MANV = ?";

        // Xử lý CCCD rỗng thành NULL
        String cccd = req.getCccd();
        if (cccd != null && cccd.trim().isEmpty()) {
            cccd = null;
        }

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement psFind = conn.prepareStatement(sqlFindUserId);
             PreparedStatement psUser = conn.prepareStatement(sqlUpdateUser);
             PreparedStatement psStaff = conn.prepareStatement(sqlUpdateStaff)) {

            psFind.setString(1, manv);
            ResultSet rs = psFind.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("USER_ID");

                psUser.setString(1, req.getHoten());
                psUser.setString(2, userId);
                psUser.executeUpdate();

                psStaff.setString(1, cccd);
                psStaff.setInt(2, req.getIsQl());
                psStaff.setString(3, req.getTrangThai());
                psStaff.setString(4, manv);
                return psStaff.executeUpdate() > 0;
            }
            return false;
        }
    }

    // 4. Xóa mềm nhân viên
    public boolean delete(String manv) throws SQLException {
        String sql = "UPDATE NHAN_VIEN SET IS_DELETED = 1, TRANG_THAI = 'ĐÃ NGHỈ' WHERE MANV = ?";
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manv);
            return ps.executeUpdate() > 0;
        }
    }
}