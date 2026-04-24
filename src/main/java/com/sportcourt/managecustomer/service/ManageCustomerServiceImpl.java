package com.sportcourt.managecustomer.service;

import com.sportcourt.auth.util.Sha256Password;
import com.sportcourt.managecustomer.dao.JdbcManageCustomerDao;
import com.sportcourt.managecustomer.dao.ManageCustomerDao;
import com.sportcourt.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.managecustomer.dto.CustomerProfile;
import com.sportcourt.managecustomer.dto.CustomerResult;
import com.sportcourt.managecustomer.dto.CustomerSummary;
import com.sportcourt.managecustomer.dto.UpdateCustomerRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ManageCustomerServiceImpl implements ManageCustomerService {
    private static final String DEFAULT_PASSWORD = "12345678";
    private static final String SYSTEM_EMAIL_SUFFIX = "@customer.local";
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
        String generatedEmail = buildGeneratedEmail(username);
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
                    generatedEmail,
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
        if (request == null || isBlank(request.hoTen()) || isBlank(request.sdt()) || isBlank(request.trangThai())) {
            return CustomerResult.fail("Vui lòng nhập đầy đủ thông tin cập nhật.");
        }

        try {
            boolean updated = manageCustomerDao.updateCustomer(maKhachHang.trim(), new UpdateCustomerRequest(
                    request.hoTen().trim(),
                    request.sdt().trim(),
                    request.trangThai().trim()
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
            return CustomerResult.ok("Xóa mềm khách hàng thành công.", null);
        } catch (SQLException e) {
            return CustomerResult.fail("Xóa khách hàng thất bại: " + mapOracleError(e));
        }
    }

    private String buildGeneratedEmail(String sdt) {
        return sdt + SYSTEM_EMAIL_SUFFIX;
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String mapOracleError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Lỗi cơ sở dữ liệu không xác định.";
        }
        if (message.contains("ORA-00001")) {
            return "Dữ liệu đã tồn tại (trùng số điện thoại hoặc tài khoản).";
        }
        if (message.contains("ORA-02290")) {
            return "Dữ liệu không đúng định dạng theo ràng buộc hệ thống.";
        }
        if (message.contains("ORA-01400")) {
            return "Thiếu dữ liệu bắt buộc.";
        }
        return message;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
