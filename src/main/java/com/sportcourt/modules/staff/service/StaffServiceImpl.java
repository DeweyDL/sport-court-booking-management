package com.sportcourt.modules.staff.service;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.staff.dao.JdbcStaffDao;
import com.sportcourt.modules.staff.dao.StaffDao;
import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.entity.Staff;
import com.sportcourt.modules.staff.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StaffServiceImpl implements StaffService {
    private static final String PHONE_PATTERN = "^0\\d{9}$";
    private static final String CCCD_PATTERN = "^\\d{12}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private final StaffDao staffDao;

    public StaffServiceImpl() {
        this(new JdbcStaffDao());
    }

    public StaffServiceImpl(StaffDao staffDao) {
        this.staffDao = staffDao;
    }

    @Override
    public List<StaffResponse> searchStaff(StaffSearchCriteria criteria) {
        try {
            return staffDao.search(criteria);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL: " + e.getMessage(), e);
        }
    }

    @Override
    public StaffDetailResponse getStaffDetail(String maNv) {
        if (isBlank(maNv)) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên.");
        }

        try {
            Optional<StaffDetailResponse> detail = staffDao.findDetailById(maNv.trim());

            if (detail.isPresent()) {
                return detail.get();
            }

            throw new IllegalArgumentException("Không tìm thấy nhân viên.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi tải chi tiết nhân viên: " + e.getMessage(), e);
        }
    }

    @Override
    public void createStaff(StaffCreateRequest request) {
        validateCreateRequest(request);

        try {
            if (staffDao.existsByPhone(request.getSdt(), null)) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại.");
            }

            if (staffDao.existsByEmail(request.getEmail(), null)) {
                throw new IllegalArgumentException("Email đã tồn tại.");
            }

            if (staffDao.existsByCccd(request.getCccd(), null)) {
                throw new IllegalArgumentException("Căn cước công dân đã tồn tại.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi kiểm tra dữ liệu nhân viên: " + e.getMessage(), e);
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            String userId = nextUserId();

            User user = new User();
            user.setUserId(userId);
            user.setHoTen(request.getHoTen().trim());
            user.setSdt(request.getSdt().trim());
            user.setEmail(request.getEmail().trim());
            user.setNgaySinh(request.getNgaySinh());
            user.setDiaChi(request.getDiaChi().trim());

            Staff staff = new Staff();
            staff.setMaNv("AUTO");
            staff.setUserId(userId);
            staff.setMaCn(request.getMaCn());
            staff.setMaLoaiNv(request.getMaLoaiNv());
            staff.setNgayVaoLam(request.getNgayVaoLam());
            staff.setCccd(request.getCccd().trim());
            staff.setQuanLy(request.isQuanLy());

            staffDao.insertUser(conn, user);
            staffDao.insertStaff(conn, staff);

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi thêm nhân viên: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void updateStaff(StaffUpdateRequest request) {
        validateUpdateRequest(request);

        try {
            if (staffDao.existsByPhone(request.getSdt(), request.getUserId())) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại.");
            }

            if (staffDao.existsByEmail(request.getEmail(), request.getUserId())) {
                throw new IllegalArgumentException("Email đã tồn tại.");
            }

            if (staffDao.existsByCccd(request.getCccd(), request.getMaNv())) {
                throw new IllegalArgumentException("Căn cước công dân đã tồn tại.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi kiểm tra dữ liệu nhân viên: " + e.getMessage(), e);
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            User user = new User();
            user.setUserId(request.getUserId());
            user.setHoTen(request.getHoTen().trim());
            user.setSdt(request.getSdt().trim());
            user.setEmail(request.getEmail().trim());
            user.setNgaySinh(request.getNgaySinh());
            user.setDiaChi(request.getDiaChi().trim());

            Staff staff = new Staff();
            staff.setMaNv(request.getMaNv());
            staff.setUserId(request.getUserId());
            staff.setMaCn(request.getMaCn());
            staff.setMaLoaiNv(request.getMaLoaiNv());
            staff.setNgayVaoLam(request.getNgayVaoLam());
            staff.setCccd(request.getCccd().trim());
            staff.setQuanLy(request.isQuanLy());

            boolean userUpdated = staffDao.updateUser(conn, user);
            boolean staffUpdated = staffDao.updateStaff(conn, staff);

            if (!userUpdated || !staffUpdated) {
                throw new IllegalArgumentException("Không thể cập nhật nhân viên.");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi cập nhật nhân viên: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void deleteStaff(String maNv) {
        if (isBlank(maNv)) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên cần xoá.");
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            boolean deleted = staffDao.softDeleteStaff(conn, maNv.trim());

            if (!deleted) {
                throw new IllegalArgumentException("Không thể xoá nhân viên.");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi xoá nhân viên: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    private void validateCreateRequest(StaffCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ.");
        }

        validateCommon(
                request.getHoTen(),
                request.getSdt(),
                request.getCccd(),
                request.getEmail(),
                request.getDiaChi(),
                request.getNgaySinh(),
                request.getNgayVaoLam()
        );
    }

    private void validateUpdateRequest(StaffUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ.");
        }

        if (isBlank(request.getMaNv())) {
            throw new IllegalArgumentException("Thiếu mã nhân viên.");
        }

        if (isBlank(request.getUserId())) {
            throw new IllegalArgumentException("Thiếu mã người dùng.");
        }

        validateCommon(
                request.getHoTen(),
                request.getSdt(),
                request.getCccd(),
                request.getEmail(),
                request.getDiaChi(),
                request.getNgaySinh(),
                request.getNgayVaoLam()
        );
    }

    private void validateCommon(
            String hoTen,
            String sdt,
            String cccd,
            String email,
            String diaChi,
            LocalDate ngaySinh,
            LocalDate ngayVaoLam
    ) {
        if (isBlank(hoTen)) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên nhân viên.");
        }

        if (isBlank(sdt) || !sdt.trim().matches(PHONE_PATTERN)) {
            throw new IllegalArgumentException("Số điện thoại phải có 10 số và bắt đầu bằng 0.");
        }

        if (isBlank(cccd) || !cccd.trim().matches(CCCD_PATTERN)) {
            throw new IllegalArgumentException("Căn cước công dân phải gồm đúng 12 số.");
        }

        if (isBlank(email) || !email.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }

        if (isBlank(diaChi)) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ.");
        }

        if (ngaySinh == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày sinh.");
        }

        if (ngayVaoLam == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày vào làm.");
        }
    }

    private String nextUserId() {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "U" + raw.substring(0, 19);
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }

        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}