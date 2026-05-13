package com.sportcourt.modules.supplier.service;

import com.sportcourt.modules.auth.dto.FunctionId;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.service.PermissionService;
import com.sportcourt.modules.auth.service.PermissionServiceImpl;
import com.sportcourt.modules.supplier.dao.JdbcSupplierManagementDAO;
import com.sportcourt.modules.supplier.dao.SupplierManagementDAO;
import com.sportcourt.modules.supplier.dto.SupplierCreateRequest;
import com.sportcourt.modules.supplier.dto.SupplierUpdateRequest;
import com.sportcourt.modules.supplier.entity.Supplier;

import java.sql.SQLException;
import java.util.List;

public class SupplierManagementServiceImpl implements SupplierManagementService {
    private static final String FUNCTION_ID = FunctionId.SUPPLIER_MANAGEMENT;

    // Matches DB constraint: CK_NCC_SDT CHECK (REGEXP_LIKE(SDT, '^0[0-9]{9}$'))
    private static final java.util.regex.Pattern SDT_PATTERN =
            java.util.regex.Pattern.compile("^0[0-9]{9}$");

    private final SupplierManagementDAO supplierManagementDAO;
    private final PermissionService permissionService;

    public SupplierManagementServiceImpl() {
        this(new JdbcSupplierManagementDAO(), new PermissionServiceImpl());
    }

    public SupplierManagementServiceImpl(SupplierManagementDAO supplierManagementDAO,
                                         PermissionService permissionService) {
        this.supplierManagementDAO = supplierManagementDAO;
        this.permissionService = permissionService;
    }

    @Override
    public List<Supplier> searchSuppliers(String keyword) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return supplierManagementDAO.findSuppliers(keyword);
    }

    @Override
    public void createSupplier(SupplierCreateRequest request) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.ADD);
        validateCreate(request);
        supplierManagementDAO.createSupplier(request.getMancc().trim(), request);
    }

    @Override
    public void updateSupplier(SupplierUpdateRequest request) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.EDIT);
        validateUpdate(request);
        boolean updated = supplierManagementDAO.updateSupplier(request);
        if (!updated) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp để cập nhật hoặc NCC đã bị xóa.");
        }
    }

    @Override
    public void deleteSupplier(String mancc) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.DELETE);
        if (mancc == null || mancc.isBlank()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ.");
        }
        boolean deleted = supplierManagementDAO.softDeleteSupplier(mancc.trim());
        if (!deleted) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp để xóa hoặc NCC đã bị xóa rồi.");
        }
    }

    @Override
    public void restoreSupplier(String mancc) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.EDIT);
        if (mancc == null || mancc.isBlank()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ.");
        }
        boolean restored = supplierManagementDAO.restoreSupplier(mancc.trim());
        if (!restored) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp để khôi phục hoặc NCC chưa bị xóa.");
        }
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private void validateCreate(SupplierCreateRequest r) {
        if (r.getMancc() == null || r.getMancc().isBlank())
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống.");
        if (r.getMancc().trim().length() > 20)
            throw new IllegalArgumentException("Mã nhà cung cấp không được vượt quá 20 ký tự.");
        if (r.getTenncc() == null || r.getTenncc().isBlank())
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        if (r.getSdt() == null || r.getSdt().isBlank())
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        if (!SDT_PATTERN.matcher(r.getSdt().trim()).matches())
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số và bắt đầu bằng 0 (VD: 0901234567).");
        if (r.getDiachi() == null || r.getDiachi().isBlank())
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
    }

    private void validateUpdate(SupplierUpdateRequest r) {
        if (r.getMancc() == null || r.getMancc().isBlank())
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ.");
        if (r.getTenncc() == null || r.getTenncc().isBlank())
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        if (r.getSdt() == null || r.getSdt().isBlank())
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        if (!SDT_PATTERN.matcher(r.getSdt().trim()).matches())
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số và bắt đầu bằng 0 (VD: 0901234567).");
        if (r.getDiachi() == null || r.getDiachi().isBlank())
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
    }
}