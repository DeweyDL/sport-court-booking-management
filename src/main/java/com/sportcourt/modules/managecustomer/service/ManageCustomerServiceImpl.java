package com.sportcourt.modules.managecustomer.service;

import com.sportcourt.modules.auth.util.Sha256Password;
import com.sportcourt.modules.managecustomer.dao.JdbcManageCustomerDao;
import com.sportcourt.modules.managecustomer.dao.ManageCustomerDao;
import com.sportcourt.modules.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.modules.managecustomer.dto.CustomerProfile;
import com.sportcourt.modules.managecustomer.dto.CustomerResult;
import com.sportcourt.modules.managecustomer.dto.CustomerSummary;
import com.sportcourt.modules.managecustomer.dto.UpdateCustomerRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ManageCustomerServiceImpl implements ManageCustomerService {
    private static final String DEFAULT_PASSWORD = "12345678";
    private final ManageCustomerDao manageCustomerDao;

    public ManageCustomerServiceImpl() {
        this(new JdbcManageCustomerDao());
    }

    public ManageCustomerServiceImpl(ManageCustomerDao manageCustomerDao) {
        this.manageCustomerDao = manageCustomerDao;
    }

    @Override
    public CustomerResult<List<CustomerSummary>> searchByName(String keyword) {
        try {
            return CustomerResult.ok("Lấy danh sách khách hàng thành công.", manageCustomerDao.findByName(keyword));
        } catch (SQLException e) {
            return CustomerResult.fail("Không thể tải danh sách khách hàng: " + e.getMessage());
        }
    }

    @Override
    public CustomerResult<CustomerProfile> getProfile(String maKhachHang) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Thiếu mã khách hàng.");
        }
        try {
            Optional<CustomerProfile> profile = manageCustomerDao.findProfileById(maKhachHang.trim());
            if (profile.isEmpty()) {
                return CustomerResult.fail("Không tìm thấy khách hàng.");
            }
            return CustomerResult.ok("Lấy profile khách hàng thành công.", profile.get());
        } catch (SQLException e) {
            return CustomerResult.fail("Không thể lấy profile khách hàng: " + e.getMessage());
        }
    }

    @Override
    public CustomerResult<CustomerProfile> createCustomer(CreateCustomerRequest request) {
        if (request == null || isBlank(request.hoTen()) || isBlank(request.sdt())) {
            return CustomerResult.fail("Vui lòng nhập đầy đủ họ tên và số điện thoại.");
        }

        String userId = generateId("USR");
        String accountId = generateId("ACC");
        String maKhachHang = generateId("KH");
        String username = request.sdt().trim();
        String passwordHash = Sha256Password.hash(DEFAULT_PASSWORD);

        try {
            CreateCustomerRequest normalized = new CreateCustomerRequest(
                    request.hoTen().trim(),
                    username
            );
            manageCustomerDao.createCustomer(
                    userId,
                    accountId,
                    maKhachHang,
                    normalized,
                    null,
                    passwordHash,
                    username
            );
            return getProfile(maKhachHang);
        } catch (SQLException e) {
            return CustomerResult.fail("Thêm khách hàng thất bại: " + mapOracleError(e));
        }
    }

    @Override
    public CustomerResult<CustomerProfile> updateCustomer(String maKhachHang, UpdateCustomerRequest request) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Thiếu mã khách hàng.");
        }
        if (request == null
                || isBlank(request.hoTen())
                || isBlank(request.sdt())
                || isBlank(request.trangThai())) {
            return CustomerResult.fail("Họ tên và số điện thoại là bắt buộc.");
        }

        try {
            boolean updated = manageCustomerDao.updateCustomer(maKhachHang.trim(), new UpdateCustomerRequest(
                    request.hoTen().trim(),
                    request.sdt().trim(),
                    request.trangThai().trim(),
                    normalizeOptional(request.emailHeThong()),
                    normalizeOptional(request.username()),
                    normalizeOptional(request.diaChi())
            ));
            if (!updated) {
                return CustomerResult.fail("Không tìm thấy khách hàng để cập nhật.");
            }
            return getProfile(maKhachHang.trim());
        } catch (SQLException e) {
            return CustomerResult.fail("Cập nhật thất bại: " + mapOracleError(e));
        }
    }

    @Override
    public CustomerResult<Void> softDeleteCustomer(String maKhachHang) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Thiếu mã khách hàng.");
        }
        try {
            boolean deleted = manageCustomerDao.softDeleteCustomer(maKhachHang.trim());
            if (!deleted) {
                return CustomerResult.fail("Không tìm thấy khách hàng để xóa.");
            }
            return CustomerResult.ok("Đã cập nhật trạng thái khách hàng.", null);
        } catch (SQLException e) {
            return CustomerResult.fail("Xóa khách hàng thất bại: " + mapOracleError(e));
        }
    }

    @Override
    public CustomerResult<Void> restoreCustomer(String maKhachHang) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Thiếu mã khách hàng.");
        }
        try {
            boolean restored = manageCustomerDao.restoreCustomer(maKhachHang.trim());
            if (!restored) {
                return CustomerResult.fail("Không tìm thấy khách hàng để khôi phục.");
            }
            return CustomerResult.ok("Đã khôi phục khách hàng.", null);
        } catch (SQLException e) {
            return CustomerResult.fail("Khôi phục khách hàng thất bại: " + mapOracleError(e));
        }
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String mapOracleError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Lỗi cơ sở dữ liệu không xác định.";
        }
        String upperMessage = message.toUpperCase();
        if (message.contains("ORA-00001")) {
            return "Dữ liệu đã tồn tại (trùng số điện thoại hoặc tài khoản).";
        }
        if (message.contains("ORA-02290")) {
            return "Dữ liệu không đúng định dạng theo ràng buộc hệ thống.";
        }
        if (message.contains("ORA-12899")) {
            if (upperMessage.contains("\"USERS\".\"SDT\"")) {
                return "Số điện thoại không hợp lệ. Vui lòng nhập đúng 10 số.";
            }
            return "Thông tin nhập vào vượt quá độ dài cho phép.";
        }
        if (message.contains("ORA-01400")) {
            return "Thiếu dữ liệu bắt buộc.";
        }
        return "Có lỗi xảy ra khi xử lý dữ liệu. Vui lòng thử lại.";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
