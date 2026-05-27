package com.sportcourt.modules.customer.service;

import com.sportcourt.modules.auth.util.Sha256Password;
import com.sportcourt.modules.customer.dao.JdbcManageCustomerDao;
import com.sportcourt.modules.customer.dao.ManageCustomerDao;
import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerResult;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;
import com.sportcourt.modules.customer.entity.KhachHang;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ManageCustomerServiceImpl implements ManageCustomerService {
    private static final String DEFAULT_PASSWORD = "12345678";
    private static final String CUSTOMER_USER_PREFIX = "USERS-";
    private static final String CUSTOMER_ACCOUNT_PREFIX = "ACC-";
    private static final String CUSTOMER_ID_PREFIX = "KH-";
    private static final String CUSTOMER_ACCOUNT_ROLE_GROUP_PREFIX = "ACCRGR-";
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
    public CustomerResult<KhachHang> findKhachHangById(String maKhachHang) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Thiếu mã khách hàng.");
        }
        try {
            Optional<KhachHang> khachHang = manageCustomerDao.findKhachHangById(maKhachHang.trim());
            if (khachHang.isEmpty()) {
                return CustomerResult.fail("Không tìm thấy khách hàng.");
            }
            return CustomerResult.ok("Lấy khách hàng thành công.", khachHang.get());
        } catch (SQLException e) {
            return CustomerResult.fail("Không thể lấy khách hàng: " + e.getMessage());
        }
    }

    @Override
    public CustomerResult<String> generateNextMaKhachHang() {
        try {
            return CustomerResult.ok(
                    "Sinh mã khách hàng thành công.",
                    generateCustomerId("KHACH_HANG", "MAKH", CUSTOMER_ID_PREFIX)
            );
        } catch (SQLException e) {
            return CustomerResult.fail("Không thể sinh mã khách hàng: " + e.getMessage());
        }
    }

    @Override
    public CustomerResult<CustomerProfile> createCustomer(CreateCustomerRequest request) {
        if (request == null) {
            return CustomerResult.fail("Chưa điền thông tin khách hàng.");
        }
        if (isBlank(request.hoTen())) {
            return CustomerResult.fail("Chưa điền họ tên.");
        }
        if (isBlank(request.sdt())) {
            return CustomerResult.fail("Chưa điền số điện thoại.");
        }

        String username = request.sdt().trim();
        String passwordHash = Sha256Password.hash(DEFAULT_PASSWORD);

        try {
            String userId = generateCustomerId("USERS", "USER_ID", CUSTOMER_USER_PREFIX);
            String accountId = generateCustomerId("ACCOUNT", "ACCOUNT_ID", CUSTOMER_ACCOUNT_PREFIX);
            String maKhachHang = isBlank(request.maKhachHang())
                    ? generateCustomerId("KHACH_HANG", "MAKH", CUSTOMER_ID_PREFIX)
                    : request.maKhachHang().trim();
            String accountRoleGroupId = generateCustomerId("ACCOUNT_ROLE_GROUP", "ACCOUNT_ROLE_GROUP_ID", CUSTOMER_ACCOUNT_ROLE_GROUP_PREFIX);
            CreateCustomerRequest normalized = new CreateCustomerRequest(
                    maKhachHang,
                    request.hoTen().trim(),
                    username
            );
            manageCustomerDao.createCustomer(
                    userId,
                    accountId,
                    maKhachHang,
                    accountRoleGroupId,
                    normalized,
                    passwordHash,
                    username
            );
            return getProfile(maKhachHang);
        } catch (SQLException e) {
            return CustomerResult.fail(mapOracleError(e));
        }
    }

    @Override
    public CustomerResult<CustomerProfile> updateCustomer(String maKhachHang, UpdateCustomerRequest request) {


        if (isBlank(request.hoTen())) {
            return CustomerResult.fail("Chưa điền họ tên.");
        }
        if (isBlank(request.sdt())) {
            return CustomerResult.fail("Chưa điền số điện thoại.");
        }

        try {
            boolean updated = manageCustomerDao.updateCustomer(maKhachHang.trim(), new UpdateCustomerRequest(
                    request.hoTen().trim(),
                    request.sdt().trim(),
                    request.trangThai().trim(),
                    normalizeOptional(request.emailHeThong()),
                    normalizeOptional(request.username()),
                    normalizeOptional(request.diaChi()),
                    request.ngaySinh()
            ));
            if (!updated) {
                return CustomerResult.fail("Khách hàng đã bị xóa, khôi phục lại để có thể cập nhật thông tin khách hàng.");
            }
            return getProfile(maKhachHang.trim());
        } catch (SQLException e) {
            return CustomerResult.fail(mapOracleError(e));
        }
    }

    @Override
    public CustomerResult<Void> softDeleteCustomer(String maKhachHang) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Chưa điền mã khách hàng.");
        }
        try {
            boolean deleted = manageCustomerDao.softDeleteCustomer(maKhachHang.trim());
            if (!deleted) {
                return CustomerResult.fail("Không tìm thấy khách hàng để xóa.");
            }

            return CustomerResult.ok("Xóa thành công.", null);
        } catch (SQLException e) {
            return CustomerResult.fail(mapOracleError(e));
        }
    }

    @Override
    public CustomerResult<Void> restoreCustomer(String maKhachHang) {
        if (isBlank(maKhachHang)) {
            return CustomerResult.fail("Chưa điền mã khách hàng.");
        }
        try {
            boolean restored = manageCustomerDao.restoreCustomer(maKhachHang.trim());
            if (!restored) {
                return CustomerResult.fail("Không tìm thấy khách hàng để khôi phục.");
            }
            return CustomerResult.ok("Đã khôi phục tài khoản khách hàng.", null);
        } catch (SQLException e) {
            return CustomerResult.fail(mapOracleError(e));
        }
    }

    private String generateCustomerId(String tableName, String idColumn, String prefix) throws SQLException {
        return manageCustomerDao.nextNumericId(tableName, idColumn, prefix);
    }

    private String mapOracleError(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            return "Lỗi cơ sở dữ liệu không xác định.";
        }
        String upperMessage = message.toUpperCase();
        if (message.contains("ORA-00001")) {
            return "Số điện thoại đã tồn tại";
        }
        if (message.contains("ORA-02290")) {
            if (upperMessage.contains("CK_USERS_SDT")) {
                return "Số điện thoại sai định dạng.";
            }
            if (upperMessage.contains("CK_USERS_EMAIL")) {
                return "Email sai định dạng.";
            }
            return "Dữ liệu sai định dạng.";
        }
        if (message.contains("ORA-12899")) {
            if (upperMessage.contains("\"USERS\".\"SDT\"")) {
                return "Số điện thoại không hợp lệ.";
            }
            return "Dữ liệu không hợp lệ.";
        }
        if (message.contains("ORA-01400")) {
            if (upperMessage.contains("\"USERS\".\"HOTEN\"")) {
                return "Chưa điền họ tên.";
            }
            if (upperMessage.contains("\"USERS\".\"SDT\"")) {
                return "Chưa điền số điện thoại.";
            }
            return "Dữ liệu không hợp lệ.";
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

