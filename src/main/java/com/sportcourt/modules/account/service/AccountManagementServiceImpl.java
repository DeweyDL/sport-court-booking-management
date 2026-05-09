package com.sportcourt.modules.account.service;

import com.sportcourt.modules.account.dao.AccountManagementDAO;
import com.sportcourt.modules.account.dao.JdbcAccountManagementDAO;
import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;
import com.sportcourt.modules.auth.dto.FunctionId;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.service.PermissionService;
import com.sportcourt.modules.auth.service.PermissionServiceImpl;
import com.sportcourt.modules.auth.util.Sha256Password;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AccountManagementServiceImpl implements AccountManagementService {
    private static final String FUNCTION_ID = FunctionId.ACCOUNT_MANAGEMENT;

    private final AccountManagementDAO accountManagementDAO;
    private final PermissionService permissionService;

    public AccountManagementServiceImpl() {
        this(new JdbcAccountManagementDAO(), new PermissionServiceImpl());
    }

    public AccountManagementServiceImpl(AccountManagementDAO accountManagementDAO, PermissionService permissionService) {
        this.accountManagementDAO = accountManagementDAO;
        this.permissionService = permissionService;
    }

    @Override
    public List<AccountRow> searchAccounts(String keyword) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return accountManagementDAO.findAccounts(keyword);
    }

    @Override
    public List<RoleGroupOption> getRoleGroups() throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.VIEW);
        return accountManagementDAO.findRoleGroupOptions();
    }

    @Override
    public void assignRoleGroup(String accountId, String groupId) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.EDIT);
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID không hợp lệ.");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("Role group không hợp lệ.");
        }
        accountManagementDAO.assignRoleGroup(accountId, groupId);
    }

    @Override
    public void createAccount(AccountUpsertRequest request) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.ADD);
        validateRequest(request, true);
        String userId = "USR_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String accountId = "ACC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String accountRoleGroupId = "ARG_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String passwordHash = Sha256Password.hash(request.getPassword().trim());
        accountManagementDAO.createAccount(userId, accountId, accountRoleGroupId, request, passwordHash);
    }

    @Override
    public void updateAccount(AccountUpsertRequest request) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.EDIT);
        validateRequest(request, false);
        boolean updated = accountManagementDAO.updateAccount(request);
        if (!updated) {
            throw new IllegalArgumentException("Không tìm thấy account để cập nhật.");
        }
    }

    @Override
    public void deleteAccount(String accountId) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.DELETE);
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID không hợp lệ.");
        }
        boolean deleted = accountManagementDAO.softDeleteAccount(accountId.trim());
        if (!deleted) {
            throw new IllegalArgumentException("Không tìm thấy account để xóa.");
        }
    }

    @Override
    public void restoreAccount(String accountId) throws SQLException {
        permissionService.requirePermission(FUNCTION_ID, PermissionAction.EDIT);
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID không hợp lệ.");
        }
        boolean restored = accountManagementDAO.restoreAccount(accountId.trim());
        if (!restored) {
            throw new IllegalArgumentException("Không tìm thấy account để khôi phục.");
        }
    }

    private void validateRequest(AccountUpsertRequest request, boolean isCreate) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu account không hợp lệ.");
        }
        if (!isCreate && (request.getAccountId() == null || request.getAccountId().isBlank())) {
            throw new IllegalArgumentException("Account ID không hợp lệ.");
        }
        if (request.getDisplayName() == null || request.getDisplayName().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        if (request.getRoleGroupId() == null || request.getRoleGroupId().isBlank()) {
            throw new IllegalArgumentException("Role group không hợp lệ.");
        }
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }
        if (isCreate && (request.getPassword() == null || request.getPassword().isBlank())) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
    }
}
