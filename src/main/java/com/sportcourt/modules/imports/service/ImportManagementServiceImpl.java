package com.sportcourt.modules.imports.service;

import com.sportcourt.modules.auth.dto.FunctionId;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.PermissionService;
import com.sportcourt.modules.auth.service.PermissionServiceImpl;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.imports.dao.ImportManagementDAO;
import com.sportcourt.modules.imports.dao.JdbcImportManagementDAO;
import com.sportcourt.modules.imports.dto.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ImportManagementServiceImpl implements ImportManagementService {
    private static final String FUNCTION_ID = FunctionId.IMPORT_MANAGEMENT;

    private final ImportManagementDAO importManagementDAO;
    private final PermissionService permissionService;

    public ImportManagementServiceImpl() {
        this(new JdbcImportManagementDAO(), new PermissionServiceImpl());
    }

    public ImportManagementServiceImpl(ImportManagementDAO importManagementDAO, PermissionService permissionService) {
        this.importManagementDAO = importManagementDAO;
        this.permissionService = permissionService;
    }

    @Override
    public List<ImportRow> searchImports(String keyword) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findImports(keyword);
    }

    @Override
    public List<ImportProductDetailDTO> getProductDetails(String manh) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findProductDetails(manh);
    }

    @Override
    public List<ImportEquipmentDetailDTO> getEquipmentDetails(String manh) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findEquipmentDetails(manh);
    }

    @Override
    public String generateNextImportId() throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.ADD);
        return importManagementDAO.generateNextImportId();
    }

    @Override
    public void createImport(ImportCreateRequest request) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.ADD);

        // Lấy mã nhân viên từ session đăng nhập
        UserSession session = SessionManager.requireSession();
        String employeeId = session.getEmployeeId();
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Tài khoản hiện tại không liên kết với nhân viên nào.");
        }
        request.setManv(employeeId);

        validateRequest(request, true);

        String manh = request.getManh() == null || request.getManh().isBlank()
                ? importManagementDAO.generateNextImportId()
                : request.getManh().trim();
        request.setManh(manh);

        BigDecimal triGia = calculateTriGia(request);
        request.setTriGia(triGia);

        importManagementDAO.createImport(request);
    }

    @Override
    public void updateImport(ImportCreateRequest request) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.EDIT);

        // Lấy mã nhân viên từ session đăng nhập
        UserSession session = SessionManager.requireSession();
        String employeeId = session.getEmployeeId();
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Tài khoản hiện tại không liên kết với nhân viên nào.");
        }
        request.setManv(employeeId);

        validateRequest(request, false);

        BigDecimal triGia = calculateTriGia(request);
        request.setTriGia(triGia);

        importManagementDAO.updateImport(request);
    }

    @Override
    public void deleteImport(String manh) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.DELETE);
        if (manh == null || manh.isBlank()) {
            throw new IllegalArgumentException("Mã nhập hàng không hợp lệ.");
        }
        boolean deleted = importManagementDAO.deleteImport(manh.trim());
        if (!deleted) {
            throw new IllegalArgumentException("Không tìm thấy phiếu nhập để xóa.");
        }
    }

    @Override
    public List<SupplierOption> getSupplierOptions() throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findSupplierOptions();
    }

    @Override
    public List<EmployeeOption> getEmployeeOptions() throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findEmployeeOptions();
    }

    @Override
    public List<ProductOption> getProductOptions() throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findProductOptions();
    }

    @Override
    public List<EquipmentOption> getEquipmentOptions() throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return importManagementDAO.findEquipmentOptions();
    }

    private void validateRequest(ImportCreateRequest request, boolean isCreate) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu phiếu nhập không hợp lệ.");
        }
        if (!isCreate && (request.getManh() == null || request.getManh().isBlank())) {
            throw new IllegalArgumentException("Mã nhập hàng không hợp lệ.");
        }
        if (request.getMancc() == null || request.getMancc().isBlank()) {
            throw new IllegalArgumentException("Nhà cung cấp không được để trống.");
        }
        if (request.getManv() == null || request.getManv().isBlank()) {
            throw new IllegalArgumentException("Nhân viên không được để trống.");
        }
        if (request.getMaChungTu() == null || request.getMaChungTu().isBlank()) {
            throw new IllegalArgumentException("Mã chứng từ không được để trống.");
        }
        boolean hasProducts = request.getProductDetails() != null && !request.getProductDetails().isEmpty();
        boolean hasEquipments = request.getEquipmentDetails() != null && !request.getEquipmentDetails().isEmpty();
        if (!hasProducts && !hasEquipments) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một chi tiết sản phẩm hoặc dụng cụ.");
        }
    }

    private BigDecimal calculateTriGia(ImportCreateRequest request) {
        BigDecimal total = BigDecimal.ZERO;

        if (request.getProductDetails() != null) {
            for (ImportProductDetailDTO detail : request.getProductDetails()) {
                BigDecimal lineTotal = detail.getDonGia()
                        .multiply(BigDecimal.valueOf(detail.getSlThucNhap()))
                        .multiply(BigDecimal.ONE.add(detail.getVat().divide(BigDecimal.valueOf(100))));
                total = total.add(lineTotal);
            }
        }

        if (request.getEquipmentDetails() != null) {
            for (ImportEquipmentDetailDTO detail : request.getEquipmentDetails()) {
                BigDecimal lineTotal = detail.getDonGia()
                        .multiply(BigDecimal.valueOf(detail.getSlThucNhap()))
                        .multiply(BigDecimal.ONE.subtract(detail.getCktm().divide(BigDecimal.valueOf(100))));
                total = total.add(lineTotal);
            }
        }

        return total;
    }
}
