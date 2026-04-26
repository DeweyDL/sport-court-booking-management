package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.entity.StaffType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffTypeDAO {

    public List<StaffType> findAllActive() {
        String sql = "SELECT MALNV, VITRI, MUCLUONG "
                + "FROM LOAI_NHAN_VIEN "
                + "WHERE IS_DELETED = 0 "
                + "ORDER BY VITRI";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            List<StaffType> result = new ArrayList<>();

            while (rs.next()) {
                StaffType staffType = new StaffType();
                staffType.setMaLoaiNv(rs.getString("MALNV"));
                staffType.setViTri(rs.getString("VITRI"));
                staffType.setMucLuong(rs.getBigDecimal("MUCLUONG"));

                result.add(staffType);
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải danh sách loại nhân viên.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    public boolean existsById(String maLoaiNv) {
        if (isBlank(maLoaiNv)) {
            return false;
        }

        String sql = "SELECT COUNT(*) "
                + "FROM LOAI_NHAN_VIEN "
                + "WHERE MALNV = ? "
                + "AND IS_DELETED = 0";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, maLoaiNv.trim());
            rs = ps.executeQuery();

            return rs.next() && rs.getLong(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra loại nhân viên.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
