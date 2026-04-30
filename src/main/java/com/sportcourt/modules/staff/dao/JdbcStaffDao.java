package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.entity.Staff;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStaffDao implements StaffDao {
    private static final String ACTIVE_STATUS = "HOẠT ĐỘNG";
    private static final String DELETED_STATUS = "ĐÃ XOÁ";

    @Override
    public List<StaffResponse> search(StaffSearchCriteria criteria) throws SQLException {
        StaffSearchCriteria safeCriteria = criteria == null ? new StaffSearchCriteria() : criteria;

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            String branchColumn = getNhanVienBranchColumn(conn);
            String statusColumn = getNhanVienStatusColumn(conn);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("nv.MANV, ");
            sql.append("nv.USER_ID, ");

            if (!isBlank(branchColumn)) {
                sql.append("nv.").append(branchColumn).append(" AS BRANCH_ID, ");
            } else {
                sql.append("CAST(NULL AS VARCHAR2(20)) AS BRANCH_ID, ");
            }

            sql.append("nv.MALNV, ");
            sql.append("nv.NVL AS NGAY_VAO_LAM, ");
            sql.append("nv.CCCD, ");
            sql.append("nv.IS_QL, ");
            sql.append("NVL(nv.IS_DELETED, 0) AS STAFF_DELETED, ");

            if (!isBlank(statusColumn)) {
                sql.append("nv.").append(statusColumn).append(" AS STAFF_STATUS, ");
            } else {
                sql.append("'").append(ACTIVE_STATUS).append("' AS STAFF_STATUS, ");
            }

            sql.append("u.HOTEN, ");
            sql.append("u.SDT, ");
            sql.append("u.EMAIL ");
            sql.append("FROM NHAN_VIEN nv ");
            sql.append("JOIN USERS u ON nv.USER_ID = u.USER_ID ");
            sql.append("WHERE NVL(u.IS_DELETED, 0) = 0 ");

            List<Object> params = new ArrayList<>();

            if (!isBlank(safeCriteria.getKeyword())) {
                String keyword = "%" + safeCriteria.getKeyword().trim() + "%";

                sql.append("AND (");
                sql.append("LOWER(nv.MANV) LIKE LOWER(?) ");
                sql.append("OR LOWER(u.HOTEN) LIKE LOWER(?) ");
                sql.append("OR u.SDT LIKE ? ");
                sql.append("OR LOWER(u.EMAIL) LIKE LOWER(?) ");
                sql.append("OR nv.CCCD LIKE ? ");
                sql.append(") ");

                params.add(keyword);
                params.add(keyword);
                params.add(keyword);
                params.add(keyword);
                params.add(keyword);
            }

            if (!isBlank(branchColumn)
                    && !isBlank(safeCriteria.getMaCn())
                    && existsBranch(conn, safeCriteria.getMaCn())) {
                sql.append("AND nv.").append(branchColumn).append(" = ? ");
                params.add(safeCriteria.getMaCn().trim());
            }

            if (!isBlank(safeCriteria.getMaLoaiNv()) && existsStaffType(conn, safeCriteria.getMaLoaiNv())) {
                sql.append("AND nv.MALNV = ? ");
                params.add(safeCriteria.getMaLoaiNv().trim());
            }

            if (safeCriteria.getQuanLy() != null) {
                sql.append("AND nv.IS_QL = ? ");
                params.add(Boolean.TRUE.equals(safeCriteria.getQuanLy()) ? 1 : 0);
            }

            sql.append("ORDER BY NVL(nv.IS_DELETED, 0), nv.CREATED_AT DESC, nv.MANV DESC");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                bindParams(ps, params);

                try (ResultSet rs = ps.executeQuery()) {
                    List<StaffResponse> result = new ArrayList<>();

                    while (rs.next()) {
                        result.add(mapStaffResponse(rs));
                    }

                    return result;
                }
            }
        }
    }

    @Override
    public Optional<StaffDetailResponse> findDetailById(String maNv) throws SQLException {
        if (isBlank(maNv)) {
            return Optional.empty();
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            String branchColumn = getNhanVienBranchColumn(conn);
            String statusColumn = getNhanVienStatusColumn(conn);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("nv.MANV, ");
            sql.append("nv.USER_ID, ");

            if (!isBlank(branchColumn)) {
                sql.append("nv.").append(branchColumn).append(" AS BRANCH_ID, ");
            } else {
                sql.append("CAST(NULL AS VARCHAR2(20)) AS BRANCH_ID, ");
            }

            sql.append("nv.MALNV, ");
            sql.append("nv.NVL AS NGAY_VAO_LAM, ");
            sql.append("nv.CCCD, ");
            sql.append("nv.IS_QL, ");

            if (!isBlank(statusColumn)) {
                sql.append("nv.").append(statusColumn).append(" AS STAFF_STATUS, ");
            } else {
                sql.append("'").append(ACTIVE_STATUS).append("' AS STAFF_STATUS, ");
            }

            sql.append("u.HOTEN, ");
            sql.append("u.NGAYSINH, ");
            sql.append("u.SDT, ");
            sql.append("u.EMAIL, ");
            sql.append("u.DIACHI ");
            sql.append("FROM NHAN_VIEN nv ");
            sql.append("JOIN USERS u ON nv.USER_ID = u.USER_ID ");
            sql.append("WHERE nv.MANV = ? ");
            sql.append("AND NVL(u.IS_DELETED, 0) = 0 ");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setString(1, maNv.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapStaffDetailResponse(rs));
                    }

                    return Optional.empty();
                }
            }
        }
    }

    @Override
    public boolean existsByPhone(String sdt, String exceptUserId) throws SQLException {
        if (isBlank(sdt)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM USERS ");
        sql.append("WHERE SDT = ? ");
        sql.append("AND NVL(IS_DELETED, 0) = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(sdt.trim());

        if (!isBlank(exceptUserId)) {
            sql.append("AND USER_ID <> ? ");
            params.add(exceptUserId.trim());
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            return count(conn, sql.toString(), params) > 0;
        }
    }

    @Override
    public boolean existsByEmail(String email, String exceptUserId) throws SQLException {
        if (isBlank(email)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM USERS ");
        sql.append("WHERE LOWER(EMAIL) = LOWER(?) ");
        sql.append("AND NVL(IS_DELETED, 0) = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(email.trim());

        if (!isBlank(exceptUserId)) {
            sql.append("AND USER_ID <> ? ");
            params.add(exceptUserId.trim());
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            return count(conn, sql.toString(), params) > 0;
        }
    }

    @Override
    public boolean existsByCccd(String cccd, String exceptMaNv) throws SQLException {
        if (isBlank(cccd)) {
            return false;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM NHAN_VIEN ");
        sql.append("WHERE CCCD = ? ");
        sql.append("AND NVL(IS_DELETED, 0) = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(cccd.trim());

        if (!isBlank(exceptMaNv)) {
            sql.append("AND MANV <> ? ");
            params.add(exceptMaNv.trim());
        }

        try (Connection conn = ConnectionUtils.getMyConnection()) {
            return count(conn, sql.toString(), params) > 0;
        }
    }

    @Override
    public boolean existsBranch(String maCn) throws SQLException {
        try (Connection conn = ConnectionUtils.getMyConnection()) {
            String nhanVienBranchColumn = getNhanVienBranchColumn(conn);

            if (isBlank(nhanVienBranchColumn)) {
                return true;
            }

            return existsBranch(conn, maCn) || !isBlank(findFirstBranchId(conn));
        }
    }

    @Override
    public boolean existsStaffType(String maLoaiNv) throws SQLException {
        try (Connection conn = ConnectionUtils.getMyConnection()) {
            if (!isBlank(maLoaiNv) && existsStaffType(conn, maLoaiNv)) {
                return true;
            }

            return !isBlank(findFirstStaffTypeId(conn));
        }
    }

    @Override
    public void insertUser(
            Connection conn,
            String userId,
            String hoTen,
            String sdt,
            String email,
            LocalDate ngaySinh,
            String diaChi
    ) throws SQLException {
        String sql = ""
                + "INSERT INTO USERS "
                + "(USER_ID, HOTEN, SDT, EMAIL, NGAYSINH, DIACHI, CREATED_AT, IS_DELETED) "
                + "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, hoTen);
            ps.setString(3, sdt);
            ps.setString(4, email);
            ps.setDate(5, ngaySinh == null ? null : Date.valueOf(ngaySinh));
            ps.setString(6, diaChi);
            ps.executeUpdate();
        }
    }

    @Override
    public void insertStaff(Connection conn, Staff staff) throws SQLException {
        String branchColumn = getNhanVienBranchColumn(conn);
        String statusColumn = getNhanVienStatusColumn(conn);
        String resolvedStaffTypeId = resolveStaffTypeId(conn, staff.getMaLoaiNv(), staff.isQuanLy());

        if (isBlank(resolvedStaffTypeId)) {
            throw new SQLException("Chưa có dữ liệu LOAI_NHAN_VIEN. Cần seed loại nhân viên trước.");
        }

        String resolvedBranchId = null;

        if (!isBlank(branchColumn)) {
            resolvedBranchId = resolveBranchId(conn, staff.getMaCn());

            if (isBlank(resolvedBranchId)) {
                throw new SQLException("Chưa có dữ liệu CHI_NHANH. Cần seed chi nhánh trước.");
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO NHAN_VIEN ");
        sql.append("(MANV, USER_ID, MALNV");

        if (!isBlank(branchColumn)) {
            sql.append(", ").append(branchColumn);
        }

        sql.append(", NVL, CCCD, IS_QL");

        if (!isBlank(statusColumn)) {
            sql.append(", ").append(statusColumn);
        }

        sql.append(", CREATED_AT, IS_DELETED) ");
        sql.append("VALUES (?, ?, ?");

        if (!isBlank(branchColumn)) {
            sql.append(", ?");
        }

        sql.append(", ?, ?, ?");

        if (!isBlank(statusColumn)) {
            sql.append(", ?");
        }

        sql.append(", SYSDATE, 0)");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;

            ps.setString(index++, nextStaffId(conn));
            ps.setString(index++, staff.getUserId());
            ps.setString(index++, resolvedStaffTypeId);

            if (!isBlank(branchColumn)) {
                ps.setString(index++, resolvedBranchId);
            }

            ps.setDate(index++, staff.getNgayVaoLam() == null ? null : Date.valueOf(staff.getNgayVaoLam()));
            ps.setString(index++, staff.getCccd());
            ps.setInt(index++, staff.isQuanLy() ? 1 : 0);

            if (!isBlank(statusColumn)) {
                ps.setString(index++, ACTIVE_STATUS);
            }

            ps.executeUpdate();
        }
    }

    @Override
    public boolean updateUser(
            Connection conn,
            String userId,
            String hoTen,
            String sdt,
            String email,
            LocalDate ngaySinh,
            String diaChi
    ) throws SQLException {
        String sql = ""
                + "UPDATE USERS "
                + "SET HOTEN = ?, SDT = ?, EMAIL = ?, NGAYSINH = ?, DIACHI = ? "
                + "WHERE USER_ID = ? "
                + "AND NVL(IS_DELETED, 0) = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, sdt);
            ps.setString(3, email);
            ps.setDate(4, ngaySinh == null ? null : Date.valueOf(ngaySinh));
            ps.setString(5, diaChi);
            ps.setString(6, userId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStaff(Connection conn, Staff staff) throws SQLException {
        String branchColumn = getNhanVienBranchColumn(conn);
        String statusColumn = getNhanVienStatusColumn(conn);
        String resolvedStaffTypeId = resolveStaffTypeId(conn, staff.getMaLoaiNv(), staff.isQuanLy());

        if (isBlank(resolvedStaffTypeId)) {
            throw new SQLException("Chưa có dữ liệu LOAI_NHAN_VIEN. Cần seed loại nhân viên trước.");
        }

        String resolvedBranchId = null;

        if (!isBlank(branchColumn)) {
            resolvedBranchId = resolveBranchId(conn, staff.getMaCn());

            if (isBlank(resolvedBranchId)) {
                throw new SQLException("Chưa có dữ liệu CHI_NHANH. Cần seed chi nhánh trước.");
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE NHAN_VIEN SET ");

        if (!isBlank(branchColumn)) {
            sql.append(branchColumn).append(" = ?, ");
        }

        sql.append("MALNV = ?, ");
        sql.append("NVL = ?, ");
        sql.append("CCCD = ?, ");
        sql.append("IS_QL = ? ");

        if (!isBlank(statusColumn)) {
            sql.append(", ").append(statusColumn).append(" = ? ");
        }

        sql.append("WHERE MANV = ? ");
        sql.append("AND NVL(IS_DELETED, 0) = 0");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;

            if (!isBlank(branchColumn)) {
                ps.setString(index++, resolvedBranchId);
            }

            ps.setString(index++, resolvedStaffTypeId);
            ps.setDate(index++, staff.getNgayVaoLam() == null ? null : Date.valueOf(staff.getNgayVaoLam()));
            ps.setString(index++, staff.getCccd());
            ps.setInt(index++, staff.isQuanLy() ? 1 : 0);

            if (!isBlank(statusColumn)) {
                ps.setString(index++, ACTIVE_STATUS);
            }

            ps.setString(index++, staff.getMaNv());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean softDeleteStaff(Connection conn, String maNv) throws SQLException {
        String statusColumn = getNhanVienStatusColumn(conn);

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE NHAN_VIEN SET IS_DELETED = 1 ");

        if (!isBlank(statusColumn)) {
            sql.append(", ").append(statusColumn).append(" = ? ");
        }

        sql.append("WHERE MANV = ? ");
        sql.append("AND NVL(IS_DELETED, 0) = 0");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;

            if (!isBlank(statusColumn)) {
                ps.setString(index++, DELETED_STATUS);
            }

            ps.setString(index++, maNv);

            return ps.executeUpdate() > 0;
        }
    }


    @Override
    public boolean restoreStaff(Connection conn, String maNv) throws SQLException {
        String statusColumn = getNhanVienStatusColumn(conn);

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE NHAN_VIEN SET IS_DELETED = 0 ");

        if (!isBlank(statusColumn)) {
            sql.append(", ").append(statusColumn).append(" = ? ");
        }

        sql.append("WHERE MANV = ? ");
        sql.append("AND NVL(IS_DELETED, 0) = 1");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;

            if (!isBlank(statusColumn)) {
                ps.setString(index++, ACTIVE_STATUS);
            }

            ps.setString(index++, maNv);

            return ps.executeUpdate() > 0;
        }
    }

    private StaffResponse mapStaffResponse(ResultSet rs) throws SQLException {
        StaffResponse response = new StaffResponse();

        boolean quanLy = rs.getInt("IS_QL") == 1;

        response.setMaNv(rs.getString("MANV"));
        response.setHoTen(rs.getString("HOTEN"));
        response.setSdt(rs.getString("SDT"));
        response.setEmail(rs.getString("EMAIL"));
        response.setMaCn(rs.getString("BRANCH_ID"));
        response.setCccd(rs.getString("CCCD"));
        response.setQuanLy(quanLy);
        boolean deleted = rs.getInt("STAFF_DELETED") == 1;
        response.setDeleted(deleted);
        response.setViTri(quanLy ? "QUẢN LÝ" : "THU NGÂN");
        response.setTrangThai(deleted ? DELETED_STATUS : normalizeStatus(rs.getString("STAFF_STATUS")));

        Date ngayVaoLam = rs.getDate("NGAY_VAO_LAM");
        response.setNgayVaoLam(ngayVaoLam == null ? null : ngayVaoLam.toLocalDate());

        return response;
    }

    private StaffDetailResponse mapStaffDetailResponse(ResultSet rs) throws SQLException {
        StaffDetailResponse response = new StaffDetailResponse();

        boolean quanLy = rs.getInt("IS_QL") == 1;

        response.setMaNv(rs.getString("MANV"));
        response.setUserId(rs.getString("USER_ID"));
        response.setHoTen(rs.getString("HOTEN"));
        response.setSdt(rs.getString("SDT"));
        response.setEmail(rs.getString("EMAIL"));
        response.setDiaChi(rs.getString("DIACHI"));
        response.setMaCn(rs.getString("BRANCH_ID"));
        response.setMaLoaiNv(rs.getString("MALNV"));
        response.setCccd(rs.getString("CCCD"));
        response.setQuanLy(quanLy);
        response.setViTri(quanLy ? "QUẢN LÝ" : "THU NGÂN");
        response.setTrangThai(normalizeStatus(rs.getString("STAFF_STATUS")));

        Date ngaySinh = rs.getDate("NGAYSINH");
        response.setNgaySinh(ngaySinh == null ? null : ngaySinh.toLocalDate());

        Date ngayVaoLam = rs.getDate("NGAY_VAO_LAM");
        response.setNgayVaoLam(ngayVaoLam == null ? null : ngayVaoLam.toLocalDate());

        return response;
    }

    private String nextStaffId(Connection conn) throws SQLException {
        String sql = ""
                + "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MANV, '[0-9]+$'))), 0) + 1 AS NEXT_NUM "
                + "FROM NHAN_VIEN "
                + "WHERE REGEXP_LIKE(MANV, '^NV[0-9]+$')";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int nextNumber = 1;

            if (rs.next()) {
                nextNumber = rs.getInt("NEXT_NUM");
            }

            return String.format("NV%02d", nextNumber);
        }
    }

    private String resolveBranchId(Connection conn, String requestedBranchId) throws SQLException {
        if (!isBlank(requestedBranchId) && existsBranch(conn, requestedBranchId)) {
            return requestedBranchId.trim();
        }

        return findFirstBranchId(conn);
    }

    private String resolveStaffTypeId(Connection conn, String requestedTypeId, boolean manager) throws SQLException {
        if (!isBlank(requestedTypeId) && existsStaffType(conn, requestedTypeId)) {
            return requestedTypeId.trim();
        }

        String byPosition = findStaffTypeIdByPosition(conn, manager);

        if (!isBlank(byPosition)) {
            return byPosition;
        }

        return findFirstStaffTypeId(conn);
    }

    private boolean existsBranch(Connection conn, String maCn) throws SQLException {
        if (isBlank(maCn)) {
            return false;
        }

        String branchIdColumn = getChiNhanhBranchColumn(conn);

        if (isBlank(branchIdColumn)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM CHI_NHANH WHERE "
                + branchIdColumn
                + " = ? AND NVL(IS_DELETED, 0) = 0";

        List<Object> params = new ArrayList<>();
        params.add(maCn.trim());

        return count(conn, sql, params) > 0;
    }

    private boolean existsStaffType(Connection conn, String maLoaiNv) throws SQLException {
        if (isBlank(maLoaiNv)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM LOAI_NHAN_VIEN WHERE MALNV = ? AND NVL(IS_DELETED, 0) = 0";

        List<Object> params = new ArrayList<>();
        params.add(maLoaiNv.trim());

        return count(conn, sql, params) > 0;
    }

    private String findFirstBranchId(Connection conn) throws SQLException {
        String branchIdColumn = getChiNhanhBranchColumn(conn);

        if (isBlank(branchIdColumn)) {
            return null;
        }

        String sql = ""
                + "SELECT BRANCH_ID FROM ("
                + "SELECT " + branchIdColumn + " AS BRANCH_ID "
                + "FROM CHI_NHANH "
                + "WHERE NVL(IS_DELETED, 0) = 0 "
                + "ORDER BY " + branchIdColumn
                + ") WHERE ROWNUM = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("BRANCH_ID") : null;
        }
    }

    private String findFirstStaffTypeId(Connection conn) throws SQLException {
        String sql = ""
                + "SELECT MALNV FROM ("
                + "SELECT MALNV FROM LOAI_NHAN_VIEN "
                + "WHERE NVL(IS_DELETED, 0) = 0 "
                + "ORDER BY MALNV"
                + ") WHERE ROWNUM = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("MALNV") : null;
        }
    }

    private String findStaffTypeIdByPosition(Connection conn, boolean manager) throws SQLException {
        if (!hasColumn(conn, "LOAI_NHAN_VIEN", "VITRI")) {
            return null;
        }

        String keyword1 = manager ? "%QUẢN%" : "%NHÂN%";
        String keyword2 = manager ? "%QUAN%" : "%NHAN%";

        String sql = ""
                + "SELECT MALNV FROM ("
                + "SELECT MALNV FROM LOAI_NHAN_VIEN "
                + "WHERE NVL(IS_DELETED, 0) = 0 "
                + "AND (UPPER(VITRI) LIKE UPPER(?) OR UPPER(VITRI) LIKE UPPER(?)) "
                + "ORDER BY MALNV"
                + ") WHERE ROWNUM = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword1);
            ps.setString(2, keyword2);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MALNV") : null;
            }
        }
    }

    private String getNhanVienBranchColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "NHAN_VIEN", "MACN")) {
            return "MACN";
        }

        if (hasColumn(conn, "NHAN_VIEN", "MACHINHANH")) {
            return "MACHINHANH";
        }

        if (hasColumn(conn, "NHAN_VIEN", "MA_CN")) {
            return "MA_CN";
        }

        if (hasColumn(conn, "NHAN_VIEN", "MA_CHI_NHANH")) {
            return "MA_CHI_NHANH";
        }

        return null;
    }

    private String getChiNhanhBranchColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "CHI_NHANH", "MACN")) {
            return "MACN";
        }

        if (hasColumn(conn, "CHI_NHANH", "MACHINHANH")) {
            return "MACHINHANH";
        }

        if (hasColumn(conn, "CHI_NHANH", "MA_CN")) {
            return "MA_CN";
        }

        if (hasColumn(conn, "CHI_NHANH", "MA_CHI_NHANH")) {
            return "MA_CHI_NHANH";
        }

        return null;
    }

    private String getNhanVienStatusColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "NHAN_VIEN", "TRANG_THAI")) {
            return "TRANG_THAI";
        }

        if (hasColumn(conn, "NHAN_VIEN", "TRANGTHAI")) {
            return "TRANGTHAI";
        }

        if (hasColumn(conn, "NHAN_VIEN", "STATUS")) {
            return "STATUS";
        }

        return null;
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();

        try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            if (rs.next()) {
                return true;
            }
        }

        String sql = ""
                + "SELECT COUNT(*) "
                + "FROM USER_TAB_COLUMNS "
                + "WHERE TABLE_NAME = ? "
                + "AND COLUMN_NAME = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase());
            ps.setString(2, columnName.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        }
    }

    private long count(Connection conn, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int index = 0; index < params.size(); index++) {
            Object value = params.get(index);

            if (value instanceof Integer) {
                ps.setInt(index + 1, (Integer) value);
            } else {
                ps.setString(index + 1, value == null ? null : value.toString());
            }
        }
    }

    private String normalizeStatus(String status) {
        if (isBlank(status)) {
            return ACTIVE_STATUS;
        }

        return status.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
