package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.entity.Staff;
import com.sportcourt.modules.staff.entity.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public List<StaffResponse> search(StaffSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new StaffSearchCriteria();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT nv.MANV, u.HOTEN, u.SDT, u.EMAIL, lnv.VITRI, ");
        sql.append("nv.MACN, cn.DIACHI AS DIACHI_CHI_NHANH, nv.NVL, nv.CCCD, nv.IS_QL ");
        sql.append("FROM NHAN_VIEN nv ");
        sql.append("JOIN USERS u ON nv.USER_ID = u.USER_ID ");
        sql.append("JOIN LOAI_NHAN_VIEN lnv ON nv.MALNV = lnv.MALNV ");
        sql.append("JOIN CHI_NHANH cn ON nv.MACN = cn.MACN ");
        sql.append("WHERE nv.IS_DELETED = 0 ");
        sql.append("AND u.IS_DELETED = 0 ");
        sql.append("AND lnv.IS_DELETED = 0 ");
        sql.append("AND cn.IS_DELETED = 0 ");

        List<Object> params = new ArrayList<>();

        if (!isBlank(criteria.getKeyword())) {
            sql.append("AND (LOWER(u.HOTEN) LIKE LOWER(?) ");
            sql.append("OR u.SDT LIKE ? ");
            sql.append("OR LOWER(u.EMAIL) LIKE LOWER(?) ");
            sql.append("OR LOWER(nv.MANV) LIKE LOWER(?) ");
            sql.append("OR nv.CCCD LIKE ?) ");

            String keyword = "%" + criteria.getKeyword().trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (!isBlank(criteria.getMaCn())) {
            sql.append("AND nv.MACN = ? ");
            params.add(criteria.getMaCn().trim());
        }

        if (!isBlank(criteria.getMaLoaiNv())) {
            sql.append("AND nv.MALNV = ? ");
            params.add(criteria.getMaLoaiNv().trim());
        }

        if (criteria.getQuanLy() != null) {
            sql.append("AND nv.IS_QL = ? ");
            params.add(criteria.getQuanLy() ? 1 : 0);
        }

        sql.append("ORDER BY nv.CREATED_AT DESC, nv.MANV DESC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql.toString());
            bindParams(ps, params);
            rs = ps.executeQuery();

            List<StaffResponse> result = new ArrayList<>();

            while (rs.next()) {
                result.add(mapStaffResponse(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tra cứu nhân viên.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    public StaffDetailResponse findDetailById(String maNv) {
        if (isBlank(maNv)) {
            return null;
        }

        String sql = "SELECT nv.MANV, nv.USER_ID, u.HOTEN, u.NGAYSINH, u.SDT, u.EMAIL, u.DIACHI, "
                + "nv.MACN, cn.DIACHI AS DIACHI_CHI_NHANH, nv.MALNV, "
                + "lnv.VITRI, lnv.MUCLUONG, nv.NVL, nv.CCCD, nv.IS_QL "
                + "FROM NHAN_VIEN nv "
                + "JOIN USERS u ON nv.USER_ID = u.USER_ID "
                + "JOIN LOAI_NHAN_VIEN lnv ON nv.MALNV = lnv.MALNV "
                + "JOIN CHI_NHANH cn ON nv.MACN = cn.MACN "
                + "WHERE nv.MANV = ? "
                + "AND nv.IS_DELETED = 0 "
                + "AND u.IS_DELETED = 0";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, maNv.trim());
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapStaffDetailResponse(rs);
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy chi tiết nhân viên.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    public boolean existsByPhone(String sdt, String exceptUserId) {
        if (isBlank(sdt)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM USERS WHERE SDT = ? AND IS_DELETED = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(sdt.trim());

        if (!isBlank(exceptUserId)) {
            sql.append("AND USER_ID <> ? ");
            params.add(exceptUserId.trim());
        }

        return count(sql.toString(), params) > 0;
    }

    public boolean existsByEmail(String email, String exceptUserId) {
        if (isBlank(email)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM USERS WHERE LOWER(EMAIL) = LOWER(?) AND IS_DELETED = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(email.trim());

        if (!isBlank(exceptUserId)) {
            sql.append("AND USER_ID <> ? ");
            params.add(exceptUserId.trim());
        }

        return count(sql.toString(), params) > 0;
    }

    public boolean existsByCccd(String cccd, String exceptMaNv) {
        if (isBlank(cccd)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM NHAN_VIEN WHERE CCCD = ? AND IS_DELETED = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(cccd.trim());

        if (!isBlank(exceptMaNv)) {
            sql.append("AND MANV <> ? ");
            params.add(exceptMaNv.trim());
        }

        return count(sql.toString(), params) > 0;
    }

    public void insertUser(Connection conn, User user) {
        String sql = "INSERT INTO USERS ("
                + "USER_ID, HOTEN, SDT, EMAIL, NGAYSINH, DIACHI, CREATED_AT, IS_DELETED"
                + ") VALUES (?, ?, ?, ?, ?, ?, SYSDATE, 0)";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getHoTen());
            ps.setString(3, user.getSdt());
            ps.setString(4, user.getEmail());
            ps.setDate(5, user.getNgaySinh() == null ? null : Date.valueOf(user.getNgaySinh()));
            ps.setString(6, user.getDiaChi());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm thông tin người dùng.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    public void insertStaff(Connection conn, Staff staff) {
        String sql = "INSERT INTO NHAN_VIEN ("
                + "MANV, MACN, MALNV, USER_ID, NVL, CCCD, IS_QL, CREATED_AT, IS_DELETED"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, 0)";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, staff.getMaNv());
            ps.setString(2, staff.getMaCn());
            ps.setString(3, staff.getMaLoaiNv());
            ps.setString(4, staff.getUserId());
            ps.setDate(5, staff.getNgayVaoLam() == null ? null : Date.valueOf(staff.getNgayVaoLam()));
            ps.setString(6, staff.getCccd());
            ps.setInt(7, staff.isQuanLy() ? 1 : 0);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể thêm nhân viên.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    public void updateUser(Connection conn, User user) {
        String sql = "UPDATE USERS "
                + "SET HOTEN = ?, SDT = ?, EMAIL = ?, NGAYSINH = ?, DIACHI = ? "
                + "WHERE USER_ID = ? "
                + "AND IS_DELETED = 0";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getHoTen());
            ps.setString(2, user.getSdt());
            ps.setString(3, user.getEmail());
            ps.setDate(4, user.getNgaySinh() == null ? null : Date.valueOf(user.getNgaySinh()));
            ps.setString(5, user.getDiaChi());
            ps.setString(6, user.getUserId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Không tìm thấy người dùng cần cập nhật.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật thông tin người dùng.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    public void updateStaff(Connection conn, Staff staff) {
        String sql = "UPDATE NHAN_VIEN "
                + "SET MACN = ?, MALNV = ?, NVL = ?, CCCD = ?, IS_QL = ? "
                + "WHERE MANV = ? "
                + "AND IS_DELETED = 0";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, staff.getMaCn());
            ps.setString(2, staff.getMaLoaiNv());
            ps.setDate(3, staff.getNgayVaoLam() == null ? null : Date.valueOf(staff.getNgayVaoLam()));
            ps.setString(4, staff.getCccd());
            ps.setInt(5, staff.isQuanLy() ? 1 : 0);
            ps.setString(6, staff.getMaNv());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Không tìm thấy nhân viên cần cập nhật.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật nhân viên.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    public void softDeleteStaff(Connection conn, String maNv) {
        String sql = "UPDATE NHAN_VIEN "
                + "SET IS_DELETED = 1 "
                + "WHERE MANV = ? "
                + "AND IS_DELETED = 0";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, maNv);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Không tìm thấy nhân viên cần xoá.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể xoá nhân viên.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    private long count(String sql, List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql);
            bindParams(ps, params);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra dữ liệu tồn tại.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);

            if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setString(i + 1, value == null ? null : value.toString());
            }
        }
    }

    private StaffResponse mapStaffResponse(ResultSet rs) throws SQLException {
        StaffResponse response = new StaffResponse();

        response.setMaNv(rs.getString("MANV"));
        response.setHoTen(rs.getString("HOTEN"));
        response.setSdt(rs.getString("SDT"));
        response.setEmail(rs.getString("EMAIL"));
        response.setViTri(rs.getString("VITRI"));
        response.setMaCn(rs.getString("MACN"));
        response.setDiaChiChiNhanh(rs.getString("DIACHI_CHI_NHANH"));

        Date ngayVaoLam = rs.getDate("NVL");
        response.setNgayVaoLam(ngayVaoLam == null ? null : ngayVaoLam.toLocalDate());

        response.setCccd(rs.getString("CCCD"));
        response.setQuanLy(rs.getInt("IS_QL") == 1);

        return response;
    }

    private StaffDetailResponse mapStaffDetailResponse(ResultSet rs) throws SQLException {
        StaffDetailResponse response = new StaffDetailResponse();

        response.setMaNv(rs.getString("MANV"));
        response.setUserId(rs.getString("USER_ID"));
        response.setHoTen(rs.getString("HOTEN"));

        Date ngaySinh = rs.getDate("NGAYSINH");
        response.setNgaySinh(ngaySinh == null ? null : ngaySinh.toLocalDate());

        response.setSdt(rs.getString("SDT"));
        response.setEmail(rs.getString("EMAIL"));
        response.setDiaChi(rs.getString("DIACHI"));

        response.setMaCn(rs.getString("MACN"));
        response.setDiaChiChiNhanh(rs.getString("DIACHI_CHI_NHANH"));

        response.setMaLoaiNv(rs.getString("MALNV"));
        response.setViTri(rs.getString("VITRI"));
        response.setMucLuong(rs.getBigDecimal("MUCLUONG"));

        Date ngayVaoLam = rs.getDate("NVL");
        response.setNgayVaoLam(ngayVaoLam == null ? null : ngayVaoLam.toLocalDate());

        response.setCccd(rs.getString("CCCD"));
        response.setQuanLy(rs.getInt("IS_QL") == 1);

        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
