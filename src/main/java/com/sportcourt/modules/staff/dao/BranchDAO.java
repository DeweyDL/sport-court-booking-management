package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.entity.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

    public List<Branch> findAllActive() {
        String sql = "SELECT MACN, DIACHI, HOTLINE "
                + "FROM CHI_NHANH "
                + "WHERE IS_DELETED = 0 "
                + "ORDER BY MACN";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            List<Branch> result = new ArrayList<>();

            while (rs.next()) {
                Branch branch = new Branch();
                branch.setMaCn(rs.getString("MACN"));
                branch.setDiaChi(rs.getString("DIACHI"));
                branch.setHotline(rs.getString("HOTLINE"));

                result.add(branch);
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải danh sách chi nhánh.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    public boolean existsById(String maCn) {
        if (isBlank(maCn)) {
            return false;
        }

        String sql = "SELECT COUNT(*) "
                + "FROM CHI_NHANH "
                + "WHERE MACN = ? "
                + "AND IS_DELETED = 0";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, maCn.trim());
            rs = ps.executeQuery();

            return rs.next() && rs.getLong(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra chi nhánh.", e);
        } finally {
            ConnectionUtils.close(conn, ps, rs);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
